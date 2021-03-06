package de.chrisgw.sportsbookingsniper.gui.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalogRepository;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import static de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil.newSportBuchungsJob;


@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplicationStateDao implements InitializingBean {

    private final Resource savedApplicationDataResource;
    private final ObjectMapper objectMapper;
    private final SportKatalogRepository sportKatalogRepository;

    private final ReentrantLock fileLock = new ReentrantLock();
    private SavedApplicationState applicationState;
    private SportKatalog sportKatalog;

    private final List<TeilnehmerListeListener> teilnehmerListeListeners = new ArrayList<>();
    private final List<SportBuchungJobListener> sportBuchungJobListeners = new ArrayList<>();


    public LocalDateTime getSaveTime() {
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

    public void addTeilnehmer(Teilnehmer teilnehmer) {
        List<Teilnehmer> newTeilnehmerListe = new ArrayList<>(getTeilnehmerListe().size() + 1);
        newTeilnehmerListe.add(teilnehmer);
        newTeilnehmerListe.addAll(getTeilnehmerListe());
        applicationState.setTeilnehmerListe(newTeilnehmerListe);
        for (TeilnehmerListeListener teilnehmerListeListener : teilnehmerListeListeners) {
            teilnehmerListeListener.onChangedTeilnehmerListe(newTeilnehmerListe);
        }
        saveApplicationData();
    }

    public List<Teilnehmer> getTeilnehmerListe() {
        return applicationState.getTeilnehmerListe();
    }

    public void updateTeilnehmerListe(List<Teilnehmer> neueTeilnehmerListe) {
        applicationState.setTeilnehmerListe(neueTeilnehmerListe);
        saveApplicationData();
        for (TeilnehmerListeListener teilnehmerListeListener : teilnehmerListeListeners) {
            teilnehmerListeListener.onChangedTeilnehmerListe(neueTeilnehmerListe);
        }
    }

    public void addTeilnehmerListeListener(TeilnehmerListeListener teilnehmerListeListener) {
        teilnehmerListeListeners.add(teilnehmerListeListener);
    }

    public void removeTeilnehmerListeListener(TeilnehmerListeListener teilnehmerListeListener) {
        teilnehmerListeListeners.remove(teilnehmerListeListener);
    }


    // watched SportAngebote

    public List<SportAngebot> getWatchedSportAngebote() {
        return applicationState.getWatchedSportAngebote();
    }


    // pending SportBuchungsJob

    public List<SportBuchungsJob> getPendingBuchungsJobs() {
        return applicationState.getPendingBuchungsJobs();
    }

    public void addSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        applicationState.getPendingBuchungsJobs().add(sportBuchungsJob);
        saveApplicationData();
        for (SportBuchungJobListener sportBuchungJobListener : sportBuchungJobListeners) {
            sportBuchungJobListener.onNewPendingSportBuchungsJob(sportBuchungsJob);
        }
    }

    public void refreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        int index = applicationState.getPendingBuchungsJobs().indexOf(sportBuchungsJob);
        if (index >= 0) {
            applicationState.getPendingBuchungsJobs().set(index, sportBuchungsJob);
            saveApplicationData();
            for (SportBuchungJobListener sportBuchungJobListener : sportBuchungJobListeners) {
                sportBuchungJobListener.onUpdatedSportBuchungsJob(sportBuchungsJob);
            }
        }
    }

    public void addSportBuchungJobListener(SportBuchungJobListener sportBuchungJobListener) {
        sportBuchungJobListeners.add(sportBuchungJobListener);
    }

    public void removeSportBuchungJobListener(SportBuchungJobListener sportBuchungJobListener) {
        sportBuchungJobListeners.remove(sportBuchungJobListener);
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


    // selectedheme

    public Theme getSelectedheme() {
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
            fileLock.lock();
            if (!savedApplicationDataResource.exists()) {
                return new SavedApplicationState();
            }
            File saveFile = savedApplicationDataResource.getFile();
            SavedApplicationState savedApplicationState = objectMapper.readValue(saveFile, SavedApplicationState.class);
            log.trace("read savedApplicationData {} from {}", savedApplicationState, saveFile);
            return savedApplicationState;
        } catch (IOException e) {
            throw new RuntimeException("Could not read savedApplicationData from " + savedApplicationDataResource, e);
        } finally {
            fileLock.unlock();
        }
    }

    public void saveApplicationData() {
        try {
            fileLock.lock();
            applicationState.setSaveTime(LocalDateTime.now());
            File saveFile = savedApplicationDataResource.getFile();
            log.trace("write applicationState {} to {}", applicationState, saveFile);
            objectMapper.writeValue(saveFile, applicationState);
        } catch (Exception e) {
            throw new RuntimeException("Could not write applicationState to file " + savedApplicationDataResource, e);
        } finally {
            fileLock.lock();
        }
    }


    @Override
    public void afterPropertiesSet() {
        applicationState = loadApplicationData();
        Locale.setDefault(applicationState.getLanguage());
        log.trace("on initialize load application data: {}", applicationState);

        // TODO remove dummy sportKatalog
        sportKatalog = SportBookingModelTestUtil.newSportKatalog();
        for (int i = 0; i < 9; i++) {
            addSportBuchungsJob(newSportBuchungsJob());
        }
    }


}
