package de.chrisgw.sportbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.chrisgw.sportbooking.gui.SavedApplicationData;
import de.chrisgw.sportbooking.model.PersonenAngaben;
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


    public SavedApplicationData getSavedApplicationData() {
        return savedApplicationData;
    }


    public void updatePersonenAngaben(PersonenAngaben personenAngaben) {
        savedApplicationData.setPersonenAngaben(personenAngaben);
        saveApplicationData();
        for (PersonenAngabenListener personenAngabenListener : personenAngabenListeners) {
            personenAngabenListener.onChangedPersonenAngaben(savedApplicationData, personenAngaben);
        }
    }

    public void addPersonenAngabenListener(PersonenAngabenListener personenAngabenListener) {
        personenAngabenListeners.add(personenAngabenListener);
    }

    public void removePersonenAngabenListener(PersonenAngabenListener personenAngabenListener) {
        personenAngabenListeners.remove(personenAngabenListener);
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


    @Override
    public void afterPropertiesSet() throws Exception {
        savedApplicationData = loadApplicationData();
    }

    @Override
    public void destroy() throws Exception {
        saveApplicationData();
    }


    public interface PersonenAngabenListener {

        void onChangedPersonenAngaben(SavedApplicationData savedApplicationData,
                PersonenAngaben changedPersonenAngaben);
    }

}
