package de.chrisgw.sportbookingsniper.gui.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
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


@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplicationStateDao implements InitializingBean {

    private final Resource savedApplicationDataResource;
    private final ObjectMapper objectMapper;

    private final ReentrantLock fileLock = new ReentrantLock();
    private SavedApplicationState applicationState;

    private final List<TeilnehmerAngabenListener> teilnehmerAngabenListeners = new ArrayList<>();
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


    // PersonenAngaben

    public TeilnehmerAngaben getTeilnehmerAngaben() {
        return applicationState.getTeilnehmerAngaben();
    }

    public void updateTeilnehmerAngaben(TeilnehmerAngaben teilnehmerAngaben) {
        applicationState.setTeilnehmerAngaben(teilnehmerAngaben);
        saveApplicationData();
        for (TeilnehmerAngabenListener teilnehmerAngabenListener : teilnehmerAngabenListeners) {
            teilnehmerAngabenListener.onChangedTeilnehmerAngaben(teilnehmerAngaben);
        }
    }

    public void addTeilnehmerAngabenListener(TeilnehmerAngabenListener teilnehmerAngabenListener) {
        teilnehmerAngabenListeners.add(teilnehmerAngabenListener);
    }

    public void removeTeilnehmerAngabenListener(TeilnehmerAngabenListener teilnehmerAngabenListener) {
        teilnehmerAngabenListeners.remove(teilnehmerAngabenListener);
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
            log.trace("write personenAngaben {} to {}", applicationState, saveFile);
            objectMapper.writeValue(saveFile, applicationState);
        } catch (Exception e) {
            throw new RuntimeException("Could not write PersonenAngaben to file " + savedApplicationDataResource, e);
        } finally {
            fileLock.lock();
        }
    }


    @Override
    public void afterPropertiesSet() {
        applicationState = loadApplicationData();
        Locale.setDefault(applicationState.getLanguage());
        log.trace("on initialize load application data: {}", applicationState);
    }


}
