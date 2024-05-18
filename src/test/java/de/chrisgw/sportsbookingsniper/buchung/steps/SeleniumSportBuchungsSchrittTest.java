package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil;
import de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLER;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newWebDriver;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class SeleniumSportBuchungsSchrittTest {

    private HszRwthAachenSportKatalogRepository sportKatalogRepository;
    private static WebDriver webDriver;


    @BeforeEach
    public void beforeEach() throws Exception {
        var chromedriverPath = Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        if (Files.exists(chromedriverPath)) {
            System.setProperty("webdriver.chrome.driver", chromedriverPath.toString());
        }
        webDriver = newWebDriver();
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (webDriver != null) {
            webDriver.close();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        sportKatalogRepository = new HszRwthAachenSportKatalogRepository();
    }


    @Test
    @Disabled("only for testing manual with debugger")
    void tryToBookEverySportAvailable() {
        int jobId = 0;
        Teilnehmer teilnehmer = SportBookingModelTestUtil.newTeilnehmer();
        SportKatalog currentSportKatalog = sportKatalogRepository.findCurrentSportKatalog();
        for (SportArt sportArt : currentSportKatalog.getSportArten()) {
            System.out.println(sportArt);
            for (SportAngebot sportAngebot : sportArt.getSportAngebote()) {
                if (shouldSkipSportAngebot(sportAngebot)) {
                    System.out.printf("\t%s skipped%n", sportAngebot);
                    continue;
                }
                SportTermin sportTermin = sportAngebot.bevorstehendeSportTermine().findFirst().orElse(null);
                var sportBuchungsJob = new SportBuchungsJob();
                sportBuchungsJob.setJobId(++jobId);
                sportBuchungsJob.setSportAngebot(sportAngebot);
                sportBuchungsJob.setSportTermin(sportTermin);
                sportBuchungsJob.setTeilnehmerListe(List.of(teilnehmer));
                System.out.printf("\t%s\t%s%n", sportBuchungsJob, sportAngebot.getKursinfoUrl());

                var sportBuchungsVersuch = newVerbindlicherBuchungsVersuch(webDriver, sportBuchungsJob);
                assertNotNull(sportBuchungsVersuch, "sportBuchungsVersuch");
                assertNotEquals(BUCHUNG_FEHLER, sportBuchungsVersuch.getStatus(), "BUCHUNG_FEHLER");
            }
        }
    }

    private boolean shouldSkipSportAngebot(SportAngebot sportAngebot) {
        String sportArtName = sportAngebot.getSportArt().getName();
        boolean skip = sportAngebot.isPasswortGesichert();
        skip = skip || sportArtName.startsWith("Extratouren ");
        skip = skip || sportArtName.endsWith(" Sucht");
        skip = skip || sportAngebot.bevorstehendeSportTermine().findFirst().isEmpty();
        return skip;
    }

}