package de.chrisgw.sportbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.chrisgw.sportbooking.gui.SavedApplicationData;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportBuchungsJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Service
@RequiredArgsConstructor
public class SavedApplicationDataService implements InitializingBean, DisposableBean {

    private final Resource savedApplicationDataResource;
    private final ObjectMapper objectMapper;

    private final ReentrantLock fileLock = new ReentrantLock();
    private SavedApplicationData savedApplicationData;

    private final List<PersonenAngabenListener> personenAngabenListeners = new ArrayList<>();
    private final List<SportBuchungJobListener> pendingSportBuchungJobListeners = new ArrayList<>();
    private final List<FinishedSportBuchungenListener> finishedSportBuchungenListeners = new ArrayList<>();


    public SavedApplicationData getSavedApplicationData() {
        return savedApplicationData;
    }


    public void updatePersonenAngaben(PersonenAngaben personenAngaben) {
        savedApplicationData.setPersonenAngaben(personenAngaben);
        saveApplicationData();
        for (PersonenAngabenListener personenAngabenListener : personenAngabenListeners) {
            personenAngabenListener.onChangedPersonenAngaben(personenAngaben);
        }
    }

    public void addPersonenAngabenListener(PersonenAngabenListener personenAngabenListener) {
        personenAngabenListeners.add(personenAngabenListener);
    }

    public void removePersonenAngabenListener(PersonenAngabenListener personenAngabenListener) {
        personenAngabenListeners.remove(personenAngabenListener);
    }


    // pending SportBuchungsJob

    public void addSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        savedApplicationData.getPendingBuchungsJobs().add(sportBuchungsJob);
        saveApplicationData();
        for (SportBuchungJobListener sportBuchungJobListener : pendingSportBuchungJobListeners) {
            sportBuchungJobListener.onAddSportBuchungsJob(sportBuchungsJob);
        }
    }

    public void refreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        int index = savedApplicationData.getPendingBuchungsJobs().indexOf(sportBuchungsJob);
        if (index >= 0) {
            savedApplicationData.getPendingBuchungsJobs().set(index, sportBuchungsJob);
            saveApplicationData();
            for (SportBuchungJobListener sportBuchungJobListener : pendingSportBuchungJobListeners) {
                sportBuchungJobListener.onRefreshSportBuchungsJob(sportBuchungsJob);
            }
        }
    }

    public void addSportBuchungJobListener(SportBuchungJobListener sportBuchungJobListener) {
        pendingSportBuchungJobListeners.add(sportBuchungJobListener);
    }

    public void removeSportBuchungJobListener(SportBuchungJobListener sportBuchungJobListener) {
        pendingSportBuchungJobListeners.remove(sportBuchungJobListener);
    }


    // finished SportBuchung

    public void addFinishedSportBuchung(SportBuchungsBestaetigung sportBuchungsBestaetigung) {
        savedApplicationData.getFinishedBuchungsJobs().add(sportBuchungsBestaetigung);
        saveApplicationData();
        for (FinishedSportBuchungenListener finishedSportBuchungenListener : finishedSportBuchungenListeners) {
            finishedSportBuchungenListener.onAddFinishedSportBuchung(sportBuchungsBestaetigung);
        }
    }

    public void addFinishedSportBuchungenListener(FinishedSportBuchungenListener finishedSportBuchungenListener) {
        finishedSportBuchungenListeners.add(finishedSportBuchungenListener);
    }

    public void removeFinishedSportBuchungenListener(FinishedSportBuchungenListener finishedSportBuchungenListener) {
        finishedSportBuchungenListeners.remove(finishedSportBuchungenListener);
    }


    public void setFirstVisite(boolean firstVisite) {
        savedApplicationData.setFirstVisite(firstVisite);
        saveApplicationData();
    }


    public SavedApplicationData loadApplicationData() {
        try {
            fileLock.lock();
            if (!savedApplicationDataResource.exists()) {
                return new SavedApplicationData();
            }
            File saveFile = savedApplicationDataResource.getFile();
            SavedApplicationData savedApplicationData = objectMapper.readValue(saveFile, SavedApplicationData.class);
            log.trace("read savedApplicationData {} from {}", savedApplicationData, saveFile);
            return savedApplicationData;
        } catch (IOException e) {
            throw new RuntimeException("Could not read savedApplicationData from " + savedApplicationDataResource, e);
        } finally {
            fileLock.unlock();
        }
    }

    public void saveApplicationData() {
        try {
            fileLock.lock();
            savedApplicationData.setSaveTime(LocalDateTime.now());
            File saveFile = savedApplicationDataResource.getFile();
            log.trace("write personenAngaben {} to {}", savedApplicationData, saveFile);
            objectMapper.writeValue(saveFile, savedApplicationData);
        } catch (Exception e) {
            throw new RuntimeException("Could not write PersonenAngaben to file " + savedApplicationDataResource, e);
        } finally {
            fileLock.lock();
        }
    }



    public void clearAll() {
        savedApplicationData.getPendingBuchungsJobs().clear();
        savedApplicationData.getFinishedBuchungsJobs().clear();
        saveApplicationData();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        savedApplicationData = loadApplicationData();
    }

    @Override
    public void destroy() throws Exception {
        saveApplicationData();
    }





    public interface PersonenAngabenListener {

        void onChangedPersonenAngaben(PersonenAngaben changedPersonenAngaben);
    }


    public interface SportBuchungJobListener {

        void onAddSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

        void onRefreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

    }


    public interface FinishedSportBuchungenListener {

        void onAddFinishedSportBuchung(SportBuchungsBestaetigung sportBuchungsBestaetigung);

    }

}
