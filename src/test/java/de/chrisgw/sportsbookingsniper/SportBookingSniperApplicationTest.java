package de.chrisgw.sportsbookingsniper;

import de.chrisgw.sportsbookingsniper.angebot.*;
import de.chrisgw.sportsbookingsniper.buchung.ScheduledSportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.*;
import de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.buchung.strategie.SportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;


public class SportBookingSniperApplicationTest {

    private final static String SPORT_ART = "Spinning Online Level 2";
    private final static String KURSNUMMER = "38815124";

    private final SportBookingSniperApplication sportBookingSniperApplication;


    public SportBookingSniperApplicationTest(SportBookingSniperApplication sportBookingSniperApplication) {
        this.sportBookingSniperApplication = sportBookingSniperApplication;
    }


    public static void main(String[] args) {
        try {
            Path chromedriverPath = Paths.get("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
            if (Files.exists(chromedriverPath)) {
                System.setProperty("webdriver.chrome.driver", chromedriverPath.toString());
            }
            SportBookingSniperApplicationTest testApplication = new SportBookingSniperApplicationTest(
                    new SportBookingSniperApplication());

            SportAngebot sportAngebot = testApplication.findSportAngebot(SPORT_ART, KURSNUMMER);
            SportTermin sportTermin = sportAngebot.bevorstehendeSportTermine()
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
            testApplication.bucheSportTermin(sportAngebot, sportTermin);
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


    private void bucheSportTermin(SportAngebot sportAngebot, SportTermin sportTermin)
            throws ExecutionException, InterruptedException {
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob();
        sportBuchungsJob.setSportAngebot(sportAngebot);
        sportBuchungsJob.setSportTermin(sportTermin);
        List<Teilnehmer> teilnehmerListe = readTeilnehmerListe();
        sportBuchungsJob.setTeilnehmerListe(teilnehmerListe);
        sportBuchungsJob.setBuchungsWiederholungsStrategie(getSportBuchungsStrategie());
        sportBuchungsJob.setPasswort("test1234");

        ApplicationStateDao applicationStateDao = sportBookingSniperApplication.getApplicationStateDao();
        ScheduledSportBuchungsJob scheduledBuchungsJob = applicationStateDao.addSportBuchungsJob(sportBuchungsJob);
        SportBuchungsBestaetigung sportBuchungsBestaetigung = scheduledBuchungsJob.get();
        System.out.println(sportBuchungsBestaetigung);
    }


    private List<Teilnehmer> readTeilnehmerListe() {
        ApplicationStateDao applicationStateDao = sportBookingSniperApplication.getApplicationStateDao();
        return applicationStateDao.getTeilnehmerListe();
    }


    private SportBuchungsStrategie getSportBuchungsStrategie() {
        return new KonfigurierbareSportBuchungsStrategie();
    }


    public static Teilnehmer createTeilnehmer() {
        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVorname("Vorname");
        teilnehmer.setNachname("Nachname");
        teilnehmer.setEmail("Email");
        teilnehmer.setGender(TeilnehmerGender.FEMALE);

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
            LocalDateTime startZeit = firstTerminDate.plusWeeks(i).atTime(18, 30);
            LocalDateTime endZeit = firstTerminDate.plusWeeks(i).atTime(20, 15);
            sportTermine.add(new SportTermin(startZeit, endZeit));
        }
        return sportTermine;
    }

}
