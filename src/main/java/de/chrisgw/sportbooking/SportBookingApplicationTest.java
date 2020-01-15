package de.chrisgw.sportbooking;

import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.PersonenAngaben.Gender;
import de.chrisgw.sportbooking.model.SportBuchungStrategieImpl.FixedPeriodTimeBuchungStrategie;
import de.chrisgw.sportbooking.model.SportTermin.SportTerminStatus;
import de.chrisgw.sportbooking.service.SavedApplicationDataService;
import de.chrisgw.sportbooking.service.SportBookingService;
import de.chrisgw.sportbooking.service.SportBookingSniperService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
        return savedApplicationDataService.getPersonenAngaben();
    }


    private SportBuchungStrategie getSportBuchungsStrategie() {
        return new FixedPeriodTimeBuchungStrategie(1, TimeUnit.MINUTES);
    }


    public static PersonenAngaben createPersonenAngaben() {
        PersonenAngaben personenAngaben = new PersonenAngaben();
        personenAngaben.setVorname("Vorname");
        personenAngaben.setNachname("Nachname");
        personenAngaben.setEmail("Email");
        personenAngaben.setGender(Gender.FEMALE);

        personenAngaben.setStreet("Street");
        personenAngaben.setOrt("Ort");

        personenAngaben.setPersonKategorie(PersonKategorie.MITARBEITER_FH);
        personenAngaben.setMitarbeiterNummer("MitarbeiterNummer");
        personenAngaben.setMatrikelnummer("Matrikelnummer");
        return personenAngaben;
    }


    public static SportAngebot createMontagsSportAngebot(SportArt sportArt) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setBsCode("bs_" + sportArt.getName());
        sportAngebot.setKursnummer("1234");
        sportAngebot.setBsKursid("bs_1234");
        sportAngebot.setUrl("http://www.badminton.de/" + sportAngebot.getKursnummer());

        sportAngebot.setDetails("Angebot Montags");
        sportAngebot.setLeitung("Leitung Montags");
        sportAngebot.setOrt("Ort Montags");
        sportAngebot.setPreis(new SportAngebotPreis(2500));
        sportAngebot.setSportTermine(createSportTermine(sportAngebot, LocalDate.of(2016, 10, 24)));
        return sportAngebot;
    }

    public static SportAngebot createFreitagsSportAngebot(SportArt sportArt) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setBsCode("bs_" + sportArt.getName());
        sportAngebot.setKursnummer("6412");
        sportAngebot.setBsKursid("bs_6412");
        sportAngebot.setUrl("http://www.badminton.de/" + sportAngebot.getKursnummer());

        sportAngebot.setDetails("Angebot Freitags");
        sportAngebot.setLeitung("Leitung Freitags");
        sportAngebot.setOrt("Ort Freitags");
        sportAngebot.setPreis(new SportAngebotPreis(1500));
        sportAngebot.setSportTermine(createSportTermine(sportAngebot, LocalDate.of(2016, 10, 21)));
        return sportAngebot;
    }


    public static Set<SportTermin> createSportTermine(SportAngebot sportAngebot, LocalDate firstTerminDate) {
        Set<SportTermin> sportTermine = new TreeSet<>();
        for (int i = 0; i < 3; i++) {
            SportTermin sportTermin = new SportTermin();
            sportTermin.setSportAngebot(sportAngebot);
            sportTermin.setStatus(SportTerminStatus.OFFEN);
            sportTermin.setStartZeit(firstTerminDate.plusWeeks(i).atTime(18, 30));
            sportTermin.setEndZeit(firstTerminDate.plusWeeks(i).atTime(20, 15));
            sportTermine.add(sportTermin);
        }
        return sportTermine;
    }


    public static SportBuchungsBestaetigung createSportBuchungsBestaetigung(SportTermin sportTermin) {
        SportBuchungsBestaetigung buchungsBestaetigung = new SportBuchungsBestaetigung();
        buchungsBestaetigung.setBuchungsNummer("5412123");
        buchungsBestaetigung.setBuchungsBestaetigungUrl(
                "http://www.badminton.de/bestätigung/" + buchungsBestaetigung.getBuchungsNummer());
        buchungsBestaetigung.setJobId(41);
        buchungsBestaetigung.setPersonenAngaben(createPersonenAngaben());
        buchungsBestaetigung.setSportTermin(sportTermin);
        buchungsBestaetigung.setBuchungsBestaetigung("buchungsBestätigung PDF content".getBytes());
        return buchungsBestaetigung;
    }

}
