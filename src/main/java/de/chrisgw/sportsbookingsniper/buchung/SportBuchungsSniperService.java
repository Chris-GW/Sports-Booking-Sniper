package de.chrisgw.sportsbookingsniper.buchung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;


@Slf4j
@Service
public class SportBuchungsSniperService {

    private final AtomicInteger jobIdCounter = new AtomicInteger();
    private final ScheduledExecutorService executorService;

    private final Map<SportBuchungsJob, CompletableFuture<SportBuchungsBestaetigung>> ausstehendeBuchungsJobs;


    public SportBuchungsSniperService() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.ausstehendeBuchungsJobs = Collections.synchronizedMap(new HashMap<>());
    }


    public boolean cancelSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        CompletableFuture<SportBuchungsBestaetigung> sportBuchungsBestaetigungFuture = ausstehendeBuchungsJobs.get(
                sportBuchungsJob);
        ausstehendeBuchungsJobs.remove(sportBuchungsJob);
        return sportBuchungsBestaetigungFuture.cancel(true);
    }


    public CompletableFuture<SportBuchungsBestaetigung> submitSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        Objects.requireNonNull(sportBuchungsJob).setJobId(jobIdCounter.incrementAndGet());
        log.info("Add new SportBuchungsJobs {}", sportBuchungsJob);

        CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture = new CompletableFuture<>();
        scheduleBookingSnipeTask(new SportBookingSniperTask(sportBuchungsJob, buchungsBestaetigungFuture));
        ausstehendeBuchungsJobs.put(sportBuchungsJob, buchungsBestaetigungFuture);
        return buchungsBestaetigungFuture;
    }


    private void scheduleBookingSnipeTask(SportBookingSniperTask bookingSniperTask) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            log.warn("could not schedule SportBookingSnipeTask, because executionService isShutdown(): {}",
                    bookingSniperTask);
            bookingSniperTask.buchungsBestaetigungFuture.cancel(true);
            return;
        }
        Duration durationTillNextCheck = bookingSniperTask.getDurationUntilNextTerminCheck();
        if (log.isDebugEnabled()) {
            LocalDateTime nextCheckTime = LocalDateTime.now().plus(durationTillNextCheck);
            String formattedCheckTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(nextCheckTime);
            log.debug("schedule SportBookingSnipeTask at {}: {}", formattedCheckTime, bookingSniperTask);
        }
        executorService.schedule(bookingSniperTask, durationTillNextCheck.toMillis(), TimeUnit.MILLISECONDS);
    }


    public Map<SportBuchungsJob, Future<SportBuchungsBestaetigung>> getAusstehendeBuchungsJobs() {
        return Collections.unmodifiableMap(ausstehendeBuchungsJobs);
    }


    private class SportBookingSniperTask implements Runnable {

        private final SportBuchungsJob buchungsJob;
        private final CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture;
        private int errorCount = 0;


        public SportBookingSniperTask(SportBuchungsJob buchungsJob,
                CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture) {
            this.buchungsJob = Objects.requireNonNull(buchungsJob);
            this.buchungsBestaetigungFuture = Objects.requireNonNull(buchungsBestaetigungFuture);
        }


        @Override
        public void run() {
            try {
                if (buchungsBestaetigungFuture.isDone()) {
                    log.warn("SportBuchungsJob is already done {} {}", buchungsJob, buchungsBestaetigungFuture);
                    cancelSportBuchungsJob(buchungsJob);
                } else if (buchungsJob.isPausiert()) {
                    log.warn("SportBuchungsJob is pausiert {} {}", buchungsJob, buchungsBestaetigungFuture);
                    cancelSportBuchungsJob(buchungsJob);
                } else if (!ausstehendeBuchungsJobs.containsKey(buchungsJob)) {
                    log.info("SportBuchungsJob is no longer needed {}", buchungsJob);
                    cancelSportBuchungsJob(buchungsJob);
                } else {
                    executeNewVerbindlicherBuchungsVersuch();
                }
            } catch (Exception e) {
                log.error("Error happens while try to complete SportBuchungsJob", e);
                if (++errorCount <= 3) {
                    log.debug("reschedule not finished SportBuchungsJob after exception: {}", buchungsJob);
                    scheduleBookingSnipeTask(this);
                } else {
                    log.warn("This SportBuchungsJob complete exceptionally and will be no longer reschedule {}", this);
                    buchungsBestaetigungFuture.completeExceptionally(e);
                    cancelSportBuchungsJob(buchungsJob);
                }
            }
        }

        private void executeNewVerbindlicherBuchungsVersuch() {
            log.info("try to final book open SportBuchungsJob {} with TeilnehmerListe {}", //
                    buchungsJob, buchungsJob.getTeilnehmerListe());
            SportBuchungsVersuch buchungsVersuch = newVerbindlicherBuchungsVersuch(buchungsJob);
            log.info("finish booking {} with SportBuchungsVersuch {}", buchungsJob, buchungsVersuch);
            if (BUCHUNG_ERFOLGREICH.equals(buchungsVersuch.getStatus())) {
                SportBuchungsBestaetigung buchungsBestaetigung = buchungsVersuch.getBuchungsBestaetigung();
                buchungsBestaetigungFuture.complete(buchungsBestaetigung);
                ausstehendeBuchungsJobs.remove(buchungsJob);
            } else if (buchungsVersuch.getStatus().canContineNextBuchungsVersuch()) {
                scheduleBookingSnipeTask(this);
            } else {
                log.warn("can not continue SportBookingSniperTask for job {} after SportBuchungsVersuch {}",
                        buchungsJob, buchungsVersuch);
                cancelSportBuchungsJob(buchungsJob);
            }
        }


        public Duration getDurationUntilNextTerminCheck() {
            return Duration.between(LocalDateTime.now(), buchungsJob.getBevorstehenderBuchungsVersuch());
        }

        public SportBuchungsJob getBuchungsJob() {
            return buchungsJob;
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
