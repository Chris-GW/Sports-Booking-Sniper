package de.chrisgw.sportsbookingsniper;

import de.chrisgw.sportsbookingsniper.angebot.*;
import de.chrisgw.sportsbookingsniper.buchung.*;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsStrategieImpl.FixedPeriodBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer.Gender;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class SportBookingSniperApplicationTest {

    private final static String SPORT_ART = "Fechten Level 2 - 3";
    private final static String KURSNUMMER = "33432242";

    private final SportBookingSniperApplication sportBookingSniperApplication;


    public SportBookingSniperApplicationTest(SportBookingSniperApplication sportBookingSniperApplication) {
        this.sportBookingSniperApplication = sportBookingSniperApplication;
    }


    public static void main(String[] args) {
        try {
            SportBookingSniperApplicationTest testApplication = new SportBookingSniperApplicationTest(
                    new SportBookingSniperApplication());
            System.setProperty("webdriver.chrome.driver",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");

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
        SportKatalogRepository sportKatalogRepository = sportBookingSniperApplication.getSportKatalogRepository();
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
        sportBuchungsJob.setSportTermin(sportTermin);
        sportBuchungsJob.setTeilnehmerListe(readTeilnehmerListe());
        sportBuchungsJob.setBuchungsStrategie(getSportBuchungsStrategie());
        sportBuchungsJob.setPasswort("test1234");

        SportBuchungsSniperService bookingSniperService = sportBookingSniperApplication.getSniperService();
        CompletableFuture<SportBuchungsBestaetigung> buchungsBestaetigungFuture = bookingSniperService.submitSportBuchungsJob(
                sportBuchungsJob);
        SportBuchungsBestaetigung sportBuchungsBestaetigung = buchungsBestaetigungFuture.get();
        System.out.println(sportBuchungsBestaetigung);
    }


    private List<Teilnehmer> readTeilnehmerListe() {
        ApplicationStateDao applicationStateDao = sportBookingSniperApplication.getApplicationStateDao();
        return applicationStateDao.getTeilnehmerListe();
    }


    private SportBuchungsStrategie getSportBuchungsStrategie() {
        return new FixedPeriodBuchungsStrategie(1, TimeUnit.MINUTES);
    }


    public static Teilnehmer createTeilnehmer() {
        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVorname("Vorname");
        teilnehmer.setNachname("Nachname");
        teilnehmer.setEmail("Email");
        teilnehmer.setGender(Gender.FEMALE);

        teilnehmer.setStreet("Street");
        teilnehmer.setOrt("Ort");

        teilnehmer.setTeilnehmerKategorie(TeilnehmerKategorie.MITARBEITER_FH);
        teilnehmer.setMitarbeiterNummer("MitarbeiterNummer");
        teilnehmer.setMatrikelnummer("Matrikelnummer");
        return teilnehmer;
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


    public static SortedSet<SportTermin> createSportTermine(SportAngebot sportAngebot, LocalDate firstTerminDate) {
        SortedSet<SportTermin> sportTermine = new TreeSet<>();
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
