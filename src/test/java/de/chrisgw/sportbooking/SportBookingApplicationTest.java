package de.chrisgw.sportbooking;

import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.SportBuchungsStrategieImpl.FixedPeriodBuchungsStrategie;
import de.chrisgw.sportbooking.model.TeilnehmerAngaben.Gender;
import de.chrisgw.sportbooking.repository.ApplicationStateDao;
import de.chrisgw.sportbooking.repository.SportKatalogRepository;
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

    private final static String SPORT_ART = "Softball Level 1 - 3";
    private final static String KURSNUMMER = "15142116";

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
            SportTermin sportTermin = sportAngebot.bevorstehendeSportTermine()
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
            testApplication.bucheSportTermin(sportTermin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private SportAngebot findSportAngebot(String sportArtName, String kursnummer) {
        SportKatalogRepository sportKatalogRepository = applicationContext.getBean(SportKatalogRepository.class);
        SportKatalog sportKatalog = sportKatalogRepository.findCurrentSportKatalog();
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
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob();
        sportBuchungsJob.setSportAngebot(sportTermin.getSportAngebot());
        sportBuchungsJob.setSportTermin(sportTermin);
        sportBuchungsJob.setTeilnehmerAngaben(readTeilnehmerAngaben());
        sportBuchungsJob.setBuchungsStrategie(getSportBuchungsStrategie());

        SportBookingSniperService bookingSniperService = applicationContext.getBean(SportBookingSniperService.class);
        CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture = bookingSniperService.submitSportBuchungsJob(
                sportBuchungsJob);
        SportBuchungsBestaetigung sportBuchungsBestaetigung = buchungsBestaetigungFuture.get();
        System.out.println(sportBuchungsBestaetigung);
    }


    private TeilnehmerAngaben readTeilnehmerAngaben() {
        ApplicationStateDao applicationStateDao = applicationContext.getBean(ApplicationStateDao.class);
        return applicationStateDao.getTeilnehmerAngaben();
    }


    private SportBuchungsStrategie getSportBuchungsStrategie() {
        return new FixedPeriodBuchungsStrategie(1, TimeUnit.MINUTES);
    }


    public static TeilnehmerAngaben createTeilnehmerAngaben() {
        TeilnehmerAngaben teilnehmerAngaben = new TeilnehmerAngaben();
        teilnehmerAngaben.setVorname("Vorname");
        teilnehmerAngaben.setNachname("Nachname");
        teilnehmerAngaben.setEmail("Email");
        teilnehmerAngaben.setGender(Gender.FEMALE);

        teilnehmerAngaben.setStreet("Street");
        teilnehmerAngaben.setOrt("Ort");

        teilnehmerAngaben.setTeilnehmerKategorie(TeilnehmerKategorie.MITARBEITER_FH);
        teilnehmerAngaben.setMitarbeiterNummer("MitarbeiterNummer");
        teilnehmerAngaben.setMatrikelnummer("Matrikelnummer");
        return teilnehmerAngaben;
    }


    public static SportAngebot createMontagsSportAngebot(SportArt sportArt) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setKursnummer("1234");
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

        sportAngebot.setKursnummer("6412");
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
            sportTermin.setStartZeit(firstTerminDate.plusWeeks(i).atTime(18, 30));
            sportTermin.setEndZeit(firstTerminDate.plusWeeks(i).atTime(20, 15));
            sportTermine.add(sportTermin);
        }
        return sportTermine;
    }

}
