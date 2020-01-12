package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportBuchungsJob;
import de.chrisgw.sportbooking.model.SportTermin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


public class SportBookingSniperService {

    private static final Logger LOG = LoggerFactory.getLogger(SportBookingSniperService.class);

    private final SportBookingService sportBookingService;
    private final AtomicLong jobIdCounter = new AtomicLong();
    private final ScheduledExecutorService executorService;

    private Map<SportBuchungsJob, CompletableFuture<SportBuchungsBestaetigung>> ausstehendeBuchungsJobs;
    private Map<SportBuchungsJob, SportBuchungsBestaetigung> beendeteBuchungsJobs;


    public SportBookingSniperService(SportBookingService sportBookingService) {
        this.sportBookingService = Objects.requireNonNull(sportBookingService);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.ausstehendeBuchungsJobs = Collections.synchronizedMap(new HashMap<>());
        this.beendeteBuchungsJobs = Collections.synchronizedMap(new HashMap<>());
    }


    public boolean cancelSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        CompletableFuture<SportBuchungsBestaetigung> sportBuchungsBestaetigungFuture = ausstehendeBuchungsJobs.get(
                sportBuchungsJob);
        ausstehendeBuchungsJobs.remove(sportBuchungsJob);
        return sportBuchungsBestaetigungFuture.cancel(true);
    }


    public CompletableFuture<SportBuchungsBestaetigung> submitSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        Objects.requireNonNull(sportBuchungsJob).setJobId(jobIdCounter.incrementAndGet());
        LOG.info("Add new SportBuchungsJobs {}", sportBuchungsJob);

        CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture = new CompletableFuture<>();
        scheduleBookingSnipeTask(new SportBookingSniperTask(sportBuchungsJob, buchungsBestaetigungFuture));
        ausstehendeBuchungsJobs.put(sportBuchungsJob, buchungsBestaetigungFuture);
        return buchungsBestaetigungFuture;
    }


    private void scheduleBookingSnipeTask(SportBookingSniperTask bookingSniperTask) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            LOG.warn("could not schedule SportBookingSnipeTask, because executionService isShutdown(): {}",
                    bookingSniperTask);
            bookingSniperTask.buchungsBestaetigungFuture.cancel(true);
            return;
        }
        Duration durationTillNextCheck = bookingSniperTask.getDurationUntilNextTerminCheck();
        if (LOG.isDebugEnabled()) {
            LocalDateTime nextCheckTime = LocalDateTime.now().plus(durationTillNextCheck);
            String formattedCheckTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(nextCheckTime);
            LOG.debug("schedule SportBookingSnipeTask at {}: {}", formattedCheckTime, bookingSniperTask);
        }
        executorService.schedule(bookingSniperTask, durationTillNextCheck.toMillis(), TimeUnit.MILLISECONDS);
    }


    public Map<SportBuchungsJob, Future<SportBuchungsBestaetigung>> getAusstehendeBuchungsJobs() {
        return Collections.unmodifiableMap(ausstehendeBuchungsJobs);
    }

    public Map<SportBuchungsJob, SportBuchungsBestaetigung> getBeendeteBuchungsJobs() {
        return Collections.unmodifiableMap(beendeteBuchungsJobs);
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
                    LOG.warn("SportBuchungsJob is already done {} {}", sportBuchungsJob, buchungsBestaetigungFuture);
                    return;
                }
                if (!ausstehendeBuchungsJobs.containsKey(sportBuchungsJob)) {
                    LOG.info("SportBuchungsJob is no longer needed {}", sportBuchungsJob);
                    buchungsBestaetigungFuture.cancel(false);
                    return;
                }
                SportTermin sportTermin = sportBuchungsJob.getSportTermin();
                LOG.debug("check sportTermin: {}", sportTermin.getName());
                if (tryToBookOpenSportTermin()) {
                    return;
                }
                SportBookingSniperService.this.scheduleBookingSnipeTask(this);
            } catch (Exception e) {
                LOG.error("Error happens while try to complete SportBuchungsJob", e);
                if (++errorCount <= 3) {
                    LOG.debug("reschedule not finished SportBuchungsJob after exception: {}", sportBuchungsJob);
                    SportBookingSniperService.this.scheduleBookingSnipeTask(this);
                } else {
                    LOG.warn("This SportBuchungsJob complete exceptionally and will be no longer reschedule {}", this);
                    buchungsBestaetigungFuture.completeExceptionally(e);
                    SportBookingSniperService.this.ausstehendeBuchungsJobs.remove(this.getSportBuchungsJob());
                }
            }
        }

        private boolean tryToBookOpenSportTermin() {
            LOG.info("try to final book open SportBuchungsJob {} with PersonenAngaben ", sportBuchungsJob,
                    sportBuchungsJob.getPersonenAngaben());
            SportBuchungsBestaetigung sportBuchungsBestaetigung = sportBookingService.verbindlichBuchen(
                    sportBuchungsJob);
            if (sportBuchungsBestaetigung == null) {
                LOG.warn("could not final book: {}", sportBuchungsJob);
                return false;
            }
            LOG.info("finish booking {} with baestaetigung {}", sportBuchungsJob, sportBuchungsBestaetigung);
            buchungsBestaetigungFuture.complete(sportBuchungsBestaetigung);
            ausstehendeBuchungsJobs.remove(sportBuchungsJob);
            beendeteBuchungsJobs.put(sportBuchungsJob, sportBuchungsBestaetigung);
            return true;
        }


        public Duration getDurationUntilNextTerminCheck() {
            return Duration.between(LocalDateTime.now(), sportBuchungsJob.getNextTimeForCheckTermin());
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
