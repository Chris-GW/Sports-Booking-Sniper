package de.chrisgw.sportsbookingsniper.buchung;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;


@Log4j2
public class SportBuchungsSniperService {

    private final ScheduledExecutorService executorService;
    private final AtomicInteger jobIdCounter = new AtomicInteger();
    private final Map<SportBuchungsJob, SportBuchungsSniperTask> ausstehendeBuchungsJobs = new ConcurrentHashMap<>();


    public SportBuchungsSniperService() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }


    public boolean cancel(SportBuchungsJob sportBuchungsJob) {
        SportBuchungsSniperTask buchungsSniperTask = ausstehendeBuchungsJobs.remove(sportBuchungsJob);
        return buchungsSniperTask.cancel(true);
    }


    public Future<SportBuchungsBestaetigung> submit(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob.getJobId() == 0) {
            sportBuchungsJob.setJobId(jobIdCounter.incrementAndGet());
            sportBuchungsJob.setBuchungsWiederholungsStrategie(sportBuchungsJob1 -> LocalDateTime.now().plusSeconds(1));
            log.info("submit new SportBuchungsJobs {}", sportBuchungsJob);
        }

        SportBuchungsSniperTask bookingSniperTask = new SportBuchungsSniperTask(sportBuchungsJob);
        ausstehendeBuchungsJobs.put(sportBuchungsJob, bookingSniperTask);
        scheduleSnipeTask(bookingSniperTask);
        return bookingSniperTask;
    }


    private void scheduleSnipeTask(SportBuchungsSniperTask bookingSniperTask) {
        if (executorService.isShutdown()) {
            log.warn("CANCEL SportBookingSnipeTask, because executionService isShutdown(): {}", bookingSniperTask);
            bookingSniperTask.cancel(true);
            return;
        }
        if (bookingSniperTask.isDone() || !bookingSniperTask.getBuchungsJob().canContinue()) {
            log.warn("CANCEL SportBookingSnipeTask, which is done or can't continue: {}", bookingSniperTask);
            bookingSniperTask.cancel(true);
            return;
        }

        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        long delayMs = bookingSniperTask.getDelay(timeUnit);
        if (log.isDebugEnabled()) {
            LocalDateTime nextCheckTime = LocalDateTime.now().plus(delayMs, timeUnit.toChronoUnit());
            String formattedCheckTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(nextCheckTime);
            log.debug("schedule SportBookingSnipeTask at {}: {}", formattedCheckTime, bookingSniperTask);
        }
        executorService.schedule(bookingSniperTask, delayMs, timeUnit);
    }


    @RequiredArgsConstructor
    private class SportBuchungsSniperTask extends CompletableFuture<SportBuchungsBestaetigung>
            implements Callable<SportBuchungsVersuch>, Delayed {

        @Getter
        private final SportBuchungsJob buchungsJob;


        @Override
        public SportBuchungsVersuch call() {
            try {
                if (isDone()) {
                    log.warn("SportBookingSniperTask is already done {} {}", buchungsJob, this);
                    cancel(true);
                    return null;
                } else {
                    return executeNewVerbindlicherBuchungsVersuch();
                }
            } catch (Exception e) {
                log.warn("This SportBuchungsJob complete exceptionally and will be no longer reschedule {}", this);
                completeExceptionally(e);
                return newBuchungsVersuch(SportBuchungsVersuchStatus.BUCHUNG_FEHLER);
            }
        }

        private SportBuchungsVersuch executeNewVerbindlicherBuchungsVersuch() {
            log.info("try to final book open SportBuchungsJob {} with TeilnehmerListe {}", //
                    buchungsJob, buchungsJob.getTeilnehmerListe());
            SportBuchungsVersuch buchungsVersuch = newVerbindlicherBuchungsVersuch(buchungsJob);
            buchungsJob.addBuchungsVersuch(buchungsVersuch);
            log.info("finish booking {} with SportBuchungsVersuch {}", buchungsJob, buchungsVersuch);

            if (BUCHUNG_ERFOLGREICH.equals(buchungsVersuch.getStatus())) {
                SportBuchungsBestaetigung buchungsBestaetigung = buchungsVersuch.getBuchungsBestaetigung();
                complete(buchungsBestaetigung);
            } else if (buchungsVersuch.getStatus().canContineNextBuchungsVersuch()) {
                scheduleSnipeTask(this);
            } else {
                log.warn("can not continue SportBookingSniperTask for job {} after SportBuchungsVersuch {}",
                        buchungsJob, buchungsVersuch);
                cancel(true);
            }
            return buchungsVersuch;
        }


        @Override
        public boolean complete(SportBuchungsBestaetigung value) {
            ausstehendeBuchungsJobs.remove(buchungsJob);
            return super.complete(value);
        }

        @Override
        public boolean completeExceptionally(Throwable ex) {
            ausstehendeBuchungsJobs.remove(buchungsJob);
            return super.completeExceptionally(ex);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            ausstehendeBuchungsJobs.remove(buchungsJob);
            return super.cancel(mayInterruptIfRunning);
        }


        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(buchungsJob.durationTillNextCheck());
        }

        @Override
        public int compareTo(Delayed delayed) {
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), delayed.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return buchungsJob.toString();
        }

    }


    public void shutdown() {
        executorService.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }


    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

}
