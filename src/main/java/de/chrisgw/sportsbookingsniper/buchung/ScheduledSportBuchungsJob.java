package de.chrisgw.sportsbookingsniper.buchung;

import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.concurrent.*;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLER;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;
import static java.util.Objects.requireNonNull;


@Log4j2
public class ScheduledSportBuchungsJob implements Future<SportBuchungsBestaetigung> {

    private final SportBuchungsJob buchungsJob;
    private final ScheduledExecutorService executorService;

    private final List<SportBuchungsJobListener> listeners = new CopyOnWriteArrayList<>();
    private final CompletableFuture<SportBuchungsBestaetigung> futureBuchungsBestaetigung = new CompletableFuture<>();
    private ScheduledFuture<SportBuchungsVersuch> scheduledBuchungsVersuch;


    public ScheduledSportBuchungsJob(SportBuchungsJob buchungsJob, ScheduledExecutorService executorService) {
        this.buchungsJob = requireNonNull(buchungsJob);
        this.executorService = requireNonNull(executorService);

        this.scheduledBuchungsVersuch = scheduleBuchungsVersuch(buchungsJob.durationTillNextCheck());
    }


    private ScheduledFuture<SportBuchungsVersuch> scheduleBuchungsVersuch(Duration delay) {
        if (delay == null || delay.isNegative()) {
            delay = Duration.ZERO;
        }
        if (executorService.isShutdown()) {
            log.warn("CANCEL ScheduledSportBuchungsJob, because executionService isShutdown(): {}", this);
            cancel(true);
            return null;

        } else if (isDone()) {
            log.warn("CANCEL ScheduledSportBuchungsJob, which is already done: {}", this);
            return null;

        } else if (!buchungsJob.canContinue()) {
            log.warn("CANCEL ScheduledSportBuchungsJob, which can't continue: {}", this);
            cancel(true);
            return null;
        }

        if (log.isDebugEnabled()) {
            LocalDateTime nextCheckTime = LocalDateTime.now().plus(delay);
            String formattedCheckTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(nextCheckTime);
            log.debug("schedule ScheduledSportBuchungsJob at {}: {}", formattedCheckTime, this);
        }
        return executorService.schedule(this::newBuchungsVersuchCall, delay.toMillis(), TimeUnit.MILLISECONDS);
    }


    public SportBuchungsVersuch newBuchungsVersuchCall() {
        try {
            if (isDone()) {
                log.warn("SportBookingSniperTask is already done {} {}", buchungsJob, this);
                return null;
            } else {
                return executeVerbindlicherBuchungsVersuch();
            }
        } catch (Exception e) {
            log.warn("This SportBuchungsJob complete exceptionally and will be no longer reschedule {}", this);
            futureBuchungsBestaetigung.completeExceptionally(e);
            return newBuchungsVersuch(BUCHUNG_FEHLER);
        }
    }

    private SportBuchungsVersuch executeVerbindlicherBuchungsVersuch() {
        log.info("try to final book open SportBuchungsJob {} with TeilnehmerListe {}", //
                buchungsJob, buchungsJob.getTeilnehmerListe());
        SportBuchungsVersuch buchungsVersuch = newVerbindlicherBuchungsVersuch(buchungsJob);
        buchungsJob.addBuchungsVersuch(buchungsVersuch);
        log.info("finish booking {} with SportBuchungsVersuch {}", buchungsJob, buchungsVersuch);

        if (BUCHUNG_ERFOLGREICH.equals(buchungsVersuch.getStatus())) {
            SportBuchungsBestaetigung buchungsBestaetigung = buchungsVersuch.getBuchungsBestaetigung();
            futureBuchungsBestaetigung.complete(buchungsBestaetigung);

        } else if (buchungsVersuch.getStatus().canContinueNextBuchungsVersuch()) {
            scheduledBuchungsVersuch = scheduleBuchungsVersuch(buchungsJob.durationTillNextCheck());

        } else {
            log.warn("can't continue SportBookingSniperTask for job {} after SportBuchungsVersuch {}", // 
                    buchungsJob, buchungsVersuch);
            cancel(true);
        }
        listeners.forEach(sportBuchungsJobListener -> sportBuchungsJobListener.onUpdatedSportBuchungsJob(buchungsJob));
        return buchungsVersuch;
    }


    public void retryNow() {
        boolean cancel = scheduledBuchungsVersuch.cancel(false);
        if (cancel) {
            scheduledBuchungsVersuch = scheduleBuchungsVersuch(Duration.ZERO);
        }
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        scheduledBuchungsVersuch.cancel(mayInterruptIfRunning);
        return futureBuchungsBestaetigung.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureBuchungsBestaetigung.isCancelled();
    }

    @Override
    public boolean isDone() {
        return futureBuchungsBestaetigung.isDone();
    }

    @Override
    public SportBuchungsBestaetigung get() throws InterruptedException, ExecutionException {
        return futureBuchungsBestaetigung.get();
    }

    @Override
    public SportBuchungsBestaetigung get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return futureBuchungsBestaetigung.get(timeout, unit);
    }


    public void addListener(SportBuchungsJobListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SportBuchungsJobListener listener) {
        listeners.remove(listener);
    }


    public SportBuchungsJob getBuchungsJob() {
        return buchungsJob;
    }

    public ScheduledFuture<SportBuchungsVersuch> getScheduledBuchungsVersuch() {
        return scheduledBuchungsVersuch;
    }


    @Override
    public String toString() {
        return buchungsJob.toString();
    }

}
