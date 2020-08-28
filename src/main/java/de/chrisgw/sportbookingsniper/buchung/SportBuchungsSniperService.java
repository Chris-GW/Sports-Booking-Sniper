package de.chrisgw.sportbookingsniper.buchung;

import de.chrisgw.sportbookingsniper.angebot.SportTermin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.chrisgw.sportbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;


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

        private final SportBuchungsJob sportBuchungsJob;
        private final CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture;
        private int errorCount = 0;


        public SportBookingSniperTask(SportBuchungsJob sportBuchungsJob,
                CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture) {
            this.sportBuchungsJob = Objects.requireNonNull(sportBuchungsJob);
            this.buchungsBestaetigungFuture = Objects.requireNonNull(buchungsBestaetigungFuture);
        }


        @Override
        public void run() {
            try {
                if (buchungsBestaetigungFuture.isDone()) {
                    log.warn("SportBuchungsJob is already done {} {}", sportBuchungsJob, buchungsBestaetigungFuture);
                    return;
                }
                if (!ausstehendeBuchungsJobs.containsKey(sportBuchungsJob)) {
                    log.info("SportBuchungsJob is no longer needed {}", sportBuchungsJob);
                    buchungsBestaetigungFuture.cancel(false);
                    return;
                }
                SportTermin sportTermin = sportBuchungsJob.getSportTermin();
                log.debug("check sportTermin: {}", sportTermin.getName());
                if (tryToBookOpenSportTermin()) {
                    return;
                }
                scheduleBookingSnipeTask(this);
            } catch (Exception e) {
                log.error("Error happens while try to complete SportBuchungsJob", e);
                if (++errorCount <= 3) {
                    log.debug("reschedule not finished SportBuchungsJob after exception: {}", sportBuchungsJob);
                    scheduleBookingSnipeTask(this);
                } else {
                    log.warn("This SportBuchungsJob complete exceptionally and will be no longer reschedule {}", this);
                    buchungsBestaetigungFuture.completeExceptionally(e);
                    ausstehendeBuchungsJobs.remove(this.getSportBuchungsJob());
                }
            }
        }

        private boolean tryToBookOpenSportTermin() {
            log.info("try to final book open SportBuchungsJob {} with TeilnehmerListe {}", //
                    sportBuchungsJob, sportBuchungsJob.getTeilnehmerListe());
            SportBuchungsVersuch buchungsVersuch = newVerbindlicherBuchungsVersuch(sportBuchungsJob);
            SportBuchungsBestaetigung buchungsBestaetigung = buchungsVersuch.getBuchungsBestaetigung();
            if (buchungsBestaetigung == null) {
                log.warn("could not final book: {}", sportBuchungsJob);
                return false;
            }
            log.info("finish booking {} with baestaetigung {}", sportBuchungsJob, buchungsBestaetigung);
            buchungsBestaetigungFuture.complete(buchungsBestaetigung);
            ausstehendeBuchungsJobs.remove(sportBuchungsJob);
            return true;
        }


        public Duration getDurationUntilNextTerminCheck() {
            return Duration.between(LocalDateTime.now(), sportBuchungsJob.getBevorstehenderBuchungsVersuch());
        }

        public SportBuchungsJob getSportBuchungsJob() {
            return sportBuchungsJob;
        }


        @Override
        public String toString() {
            return sportBuchungsJob.toString();
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
