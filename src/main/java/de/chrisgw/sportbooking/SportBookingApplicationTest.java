package de.chrisgw.sportbooking;

import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.SportBuchungStrategieImpl.FixedPeriodTimeBuchungStrategie;
import de.chrisgw.sportbooking.service.SavedApplicationDataService;
import de.chrisgw.sportbooking.service.SportBookingService;
import de.chrisgw.sportbooking.service.SportBookingSniperService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class SportBookingApplicationTest {

    private final static String SPORT_ART = "Badminton Spielbetrieb";
    private final static String KURSNUMMER = "11132976";

    private final ConfigurableApplicationContext applicationContext;


    public SportBookingApplicationTest(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public static void main(String[] args) {
        try (ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SportBookingApplication.class)) {
            SportBookingApplicationTest testApplication = new SportBookingApplicationTest(applicationContext);
            System.setProperty("webdriver.chrome.driver", "C:\\01_Programmieren\\chromedriver.exe");

            SportAngebot sportAngebot = testApplication.findSportAngebot(SPORT_ART, KURSNUMMER);
            SportTermin sportTermin = sportAngebot.getUpcomingSportTermine().get(0);
            testApplication.bucheSportTermin(sportTermin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private SportAngebot findSportAngebot(String sportArtName, String kursnummer) {
        SportBookingService bookingService = applicationContext.getBean(SportBookingService.class);
        SportKatalog sportKatalog = bookingService.loadSportKatalog();
        Optional<SportArt> sportArt = sportKatalog.findSportArtByName(sportArtName);
        if (sportArt.isPresent()) {
            Optional<SportAngebot> sportAngebot = sportArt.get().findSportAngebot(kursnummer);
            if (sportAngebot.isPresent()) {
                return sportAngebot.get();
            }
        }
        throw new RuntimeException(
                "Could not find SportTermin from SportArt=" + sportArtName + ", kursnummer=" + kursnummer);
    }

    private SportTermin findSportTermin(String sportArtName, String kursnummer, LocalDate date) {
        SportAngebot sportAngebot = findSportAngebot(sportArtName, kursnummer);
        Optional<SportTermin> sportTermin = sportAngebot.findByDate(date);
        if (sportTermin.isPresent()) {
            return sportTermin.get();
        }
        throw new RuntimeException(
                "Could not find SportTermin from SportArt=" + sportArtName + ", kursnummer=" + kursnummer + ", date="
                        + date);
    }


    private void bucheSportTermin(SportTermin sportTermin) throws ExecutionException, InterruptedException {
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob(sportTermin, readPersonenAngaben());
        sportBuchungsJob.setSportBuchungStrategie(getSportBuchungsStrategie());

        SportBookingSniperService bookingSniperService = applicationContext.getBean(SportBookingSniperService.class);
        CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture = bookingSniperService.submitSportBuchungsJob(
                sportBuchungsJob);
        SportBuchungsBestaetigung sportBuchungsBestaetigung = buchungsBestaetigungFuture.get();
        System.out.println(sportBuchungsBestaetigung);
    }


    private PersonenAngaben readPersonenAngaben() {
        SavedApplicationDataService savedApplicationDataService = applicationContext.getBean(
                SavedApplicationDataService.class);
        return savedApplicationDataService.getSavedApplicationData().getPersonenAngaben();
    }


    private SportBuchungStrategie getSportBuchungsStrategie() {
        return new FixedPeriodTimeBuchungStrategie(1, TimeUnit.MINUTES);
    }

}
