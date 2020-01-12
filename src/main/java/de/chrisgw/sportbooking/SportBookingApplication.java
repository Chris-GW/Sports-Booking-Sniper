package de.chrisgw.sportbooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.chrisgw.sportbooking.gui.SportBookingGui;
import de.chrisgw.sportbooking.model.SportKatalog;
import de.chrisgw.sportbooking.service.SportBookingService;
import de.chrisgw.sportbooking.service.SportBookingSniperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@RequiredArgsConstructor
public class SportBookingApplication {

    public static final Path PERSONEN_ANGABEN_PATH = Paths.get("PersonenAngaben.json");

    private final SportBookingService sportBookginService;
    private final SportBookingSniperService sportBookingSniperService;
    private final ObjectMapper objectMapper;
    private final SportBookingGui sportBookingGui;

    private SportKatalog sportKatalog;


    private void startGui() throws IOException {
        sportBookingGui.turnOn();
    }


    // getter / setter

    public SportKatalog getSportKatalog() {
        if (sportKatalog == null) {
            sportKatalog = sportBookginService.loadSportKatalog();
        }
        return sportKatalog;
    }


    // MAIN

    public static void main(String[] args) {
        log.trace("Start SportBookingApplication ...");
        try (ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SportBookingApplicationConfiguration.class)) {
            SportBookingApplication sportBookingApplication = applicationContext.getBean(SportBookingApplication.class);
            sportBookingApplication.startGui();
        } catch (Exception e) {
            log.error("Exception happens", e);
            e.printStackTrace();
        }
    }

}
