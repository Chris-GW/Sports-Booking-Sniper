package de.chrisgw.sportsbookingsniper.gui.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalogRepository;
import de.chrisgw.sportsbookingsniper.buchung.ScheduledSportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;


@Log4j2
public class ApplicationStateDao {

    private static final Path SAVE_PATH = Paths.get("savedSportBookingApplicationData.json").toAbsolutePath();
    private static final ReentrantLock SAVE_FILE_LOCK = new ReentrantLock();

    private final ObjectMapper objectMapper;
    private final SportKatalogRepository sportKatalogRepository;

    private final ScheduledExecutorService executorService;
    private final Map<Integer, ScheduledSportBuchungsJob> scheduledSportBuchungsJobMap = new ConcurrentHashMap<>();

    private final List<TeilnehmerListeListener> teilnehmerListeListeners = new CopyOnWriteArrayList<>();
    private final List<SportBuchungsJobListener> sportBuchungsJobListeners = new CopyOnWriteArrayList<>();

    private SportKatalog sportKatalog;
    private final SavedApplicationState applicationState;


    public ApplicationStateDao(SportKatalogRepository sportKatalogRepository, ObjectMapper objectMapper,
            ScheduledExecutorService executorService) {
        this.sportKatalogRepository = requireNonNull(sportKatalogRepository);
        this.objectMapper = requireNonNull(objectMapper);
        this.executorService = requireNonNull(executorService);
        this.applicationState = loadApplicationData();

        for (SportBuchungsJob pendingBuchungsJob : this.applicationState.getPendingBuchungsJobs()) {
            var scheduledBuchungsJob = new ScheduledSportBuchungsJob(pendingBuchungsJob, executorService);
            scheduledSportBuchungsJobMap.put(pendingBuchungsJob.getJobId(), scheduledBuchungsJob);
        }
    }


    public Instant getSaveTime() {
        return applicationState.getSaveTime();
    }


    public Locale getLanguage() {
        return applicationState.getLanguage();
    }

    public void setLanguage(Locale language) {
        applicationState.setLanguage(language);
        Locale.setDefault(language);
        saveApplicationData();
    }


    public synchronized SportKatalog currentSportKatalog() {
        if (sportKatalog == null) {
            sportKatalog = sportKatalogRepository.findCurrentSportKatalog();
            // sportKatalog = SportBookingModelTestUtil.newSportKatalog();
        }
        return sportKatalog;
    }


    // Teilnehmer

    public Teilnehmer getDefaultTeilnehmer() {
        List<Teilnehmer> teilnehmerListe = getTeilnehmerListe();
        if (teilnehmerListe.isEmpty()) {
            return null;
        }
        return teilnehmerListe.get(0);
    }

    public void setDefaultTeilnehmer(Teilnehmer teilnehmer) {
        List<Teilnehmer> teilnehmerListe = applicationState.getTeilnehmerListe();
        teilnehmerListe.remove(teilnehmer);
        teilnehmerListe.add(0, teilnehmer);
        notifyTeilnehmerListeListeners();
    }


    public boolean addTeilnehmer(Teilnehmer teilnehmer) {
        boolean add = applicationState.getTeilnehmerListe().add(teilnehmer);
        notifyTeilnehmerListeListeners();
        return add;
    }

    public void updateTeilnehmer(int index, Teilnehmer teilnehmer) {
        applicationState.getTeilnehmerListe().set(index, teilnehmer);
        notifyTeilnehmerListeListeners();
    }

    public boolean removeTeilnehmer(Teilnehmer teilnehmer) {
        boolean remove = applicationState.getTeilnehmerListe().remove(teilnehmer);
        notifyTeilnehmerListeListeners();
        return remove;
    }

    public List<Teilnehmer> getTeilnehmerListe() {
        return unmodifiableList(applicationState.getTeilnehmerListe());
    }


    public void addTeilnehmerListener(TeilnehmerListeListener teilnehmerListeListener) {
        teilnehmerListeListeners.add(teilnehmerListeListener);
    }

    public void removeTeilnehmerListener(TeilnehmerListeListener teilnehmerListeListener) {
        teilnehmerListeListeners.remove(teilnehmerListeListener);
    }

    private void notifyTeilnehmerListeListeners() {
        for (TeilnehmerListeListener teilnehmerListeListener : teilnehmerListeListeners) {
            teilnehmerListeListener.onChangedTeilnehmerListe(getTeilnehmerListe());
        }
        saveApplicationData();
    }


    // watched SportAngebote

    public List<SportAngebot> getWatchedSportAngebote() {
        return unmodifiableList(applicationState.getWatchedSportAngebote());
    }


    // pending SportBuchungsJob

    public List<SportBuchungsJob> getPendingBuchungsJobs() {
        return unmodifiableList(applicationState.getPendingBuchungsJobs());
    }


    public ScheduledSportBuchungsJob addSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        var scheduledSportBuchungsJob = getScheduledSportBuchungsJob(sportBuchungsJob);
        if (scheduledSportBuchungsJob != null) {
            return scheduledSportBuchungsJob;
        }
        if (sportBuchungsJob.getJobId() == 0) {
            synchronized (this) {
                sportBuchungsJob.setJobId(applicationState.nextJobId());
            }
        }
        applicationState.getPendingBuchungsJobs().add(sportBuchungsJob);
        var scheduledBuchungsJob = new ScheduledSportBuchungsJob(sportBuchungsJob, executorService);
        scheduledSportBuchungsJobMap.put(sportBuchungsJob.getJobId(), scheduledBuchungsJob);

        for (SportBuchungsJobListener sportBuchungsJobListener : sportBuchungsJobListeners) {
            scheduledBuchungsJob.addListener(sportBuchungsJobListener);
            sportBuchungsJobListener.onNewPendingSportBuchungsJob(sportBuchungsJob);
        }
        saveApplicationData();
        return scheduledBuchungsJob;
    }


    public ScheduledSportBuchungsJob getScheduledSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        return getScheduledSportBuchungsJob(sportBuchungsJob.getJobId());
    }

    public ScheduledSportBuchungsJob getScheduledSportBuchungsJob(int jobId) {
        return scheduledSportBuchungsJobMap.get(jobId);
    }


    public ScheduledSportBuchungsJob retrySportBuchungsJob(SportBuchungsJob buchungsJob) {
        var scheduledBuchungsJob = new ScheduledSportBuchungsJob(buchungsJob, executorService);
        for (SportBuchungsJobListener sportBuchungsJobListener : sportBuchungsJobListeners) {
            scheduledBuchungsJob.addListener(sportBuchungsJobListener);
            sportBuchungsJobListener.onNewPendingSportBuchungsJob(buchungsJob);
        }
        saveApplicationData();
        return scheduledBuchungsJob;
    }

    public void refreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        int index = applicationState.getPendingBuchungsJobs().indexOf(sportBuchungsJob);
        if (index >= 0) {
            applicationState.getPendingBuchungsJobs().set(index, sportBuchungsJob);
            for (SportBuchungsJobListener sportBuchungsJobListener : sportBuchungsJobListeners) {
                sportBuchungsJobListener.onUpdatedSportBuchungsJob(sportBuchungsJob);
            }
            saveApplicationData();
        }
    }

    public void removeSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        applicationState.getPendingBuchungsJobs().remove(sportBuchungsJob);
        int jobId = sportBuchungsJob.getJobId();
        ScheduledSportBuchungsJob scheduledSportBuchungsJob = scheduledSportBuchungsJobMap.remove(jobId);
        if (scheduledSportBuchungsJob != null) {
            scheduledSportBuchungsJob.cancel(false);
        }
        for (SportBuchungsJobListener sportBuchungsJobListener : sportBuchungsJobListeners) {
            sportBuchungsJobListener.onFinishSportBuchungJob(sportBuchungsJob);
        }
        saveApplicationData();
    }


    public void addSportBuchungsJobListener(SportBuchungsJobListener sportBuchungsJobListener) {
        sportBuchungsJobListeners.add(sportBuchungsJobListener);
    }

    public void removeSportBuchungsJobListener(SportBuchungsJobListener sportBuchungsJobListener) {
        sportBuchungsJobListeners.remove(sportBuchungsJobListener);
    }


    // finished SportBuchung

    public List<SportBuchungsJob> getFinishedBuchungsJobs() {
        return applicationState.getFinishedBuchungsJobs();
    }


    // firstVisite

    public boolean isFirstVisite() {
        return applicationState.isFirstVisite();
    }

    public void setFirstVisite(boolean firstVisite) {
        applicationState.setFirstVisite(firstVisite);
        saveApplicationData();
    }


    // selectedTheme

    public Theme getSelectedTheme() {
        String selectedTheme = applicationState.getSelectedTheme();
        if (LanternaThemes.getRegisteredThemes().contains(selectedTheme)) {
            return LanternaThemes.getRegisteredTheme(selectedTheme);
        } else {
            log.warn("theme '{}' not registered with lanterna. Fallback to default theme", selectedTheme);
            return LanternaThemes.getDefaultTheme();
        }
    }

    public void setSelectedTheme(String themeName) {
        if (LanternaThemes.getRegisteredThemes().contains(themeName)) {
            applicationState.setSelectedTheme(themeName);
            saveApplicationData();
        } else {
            log.warn("theme '{}' not registered with lanterna", themeName);
        }
    }


    public SavedApplicationState loadApplicationData() {
        try {
            SAVE_FILE_LOCK.lock();
            if (!Files.isReadable(SAVE_PATH)) {
                return new SavedApplicationState();
            }
            var savedApplicationState = objectMapper.readValue(SAVE_PATH.toFile(), SavedApplicationState.class);
            log.trace("read savedApplicationData {} from {}", savedApplicationState, SAVE_PATH);
            SportKatalog sportKatalog = currentSportKatalog();
            for (SportBuchungsJob pendingBuchungsJob : savedApplicationState.getPendingBuchungsJobs()) {
                SportAngebot readSportAngebot = pendingBuchungsJob.getSportAngebot();
                // join readSportAngebot with current SportKatalog available SportAngebot
                sportKatalog.findSportAngebot(readSportAngebot).ifPresent(pendingBuchungsJob::setSportAngebot);
            }
            return savedApplicationState;
        } catch (IOException e) {
            throw new RuntimeException("Could not read savedApplicationData from " + SAVE_PATH, e);
        } finally {
            SAVE_FILE_LOCK.unlock();
        }
    }

    public void saveApplicationData() {
        try {
            SAVE_FILE_LOCK.lock();
            applicationState.setSaveTime(Instant.now());
            log.trace("write applicationState {} to {}", applicationState, SAVE_PATH);
            objectMapper.writeValue(SAVE_PATH.toFile(), applicationState);
        } catch (Exception e) {
            throw new RuntimeException("Could not write applicationState to file " + SAVE_PATH, e);
        } finally {
            SAVE_FILE_LOCK.lock();
        }
    }

}
