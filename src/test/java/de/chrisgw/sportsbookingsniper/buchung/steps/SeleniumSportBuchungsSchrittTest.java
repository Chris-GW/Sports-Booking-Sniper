package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil;
import de.chrisgw.sportsbookingsniper.angebot.*;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import org.junit.*;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLER;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newVerbindlicherBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.buchung.steps.SeleniumSportBuchungsSchritt.newWebDriver;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class SeleniumSportBuchungsSchrittTest {

    private HszRwthAachenSportKatalogRepository sportKatalogRepository;
    private static WebDriver webDriver;


    @BeforeClass
    public static void beforeClass() throws Exception {
        var chromedriverPath = Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        if (Files.exists(chromedriverPath)) {
            System.setProperty("webdriver.chrome.driver", chromedriverPath.toString());
        }
        webDriver = newWebDriver();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (webDriver != null) {
            webDriver.close();
        }
    }

    @Before
    public void setUp() throws Exception {
        sportKatalogRepository = new HszRwthAachenSportKatalogRepository();
    }


    @Test
    @Ignore
    public void tryToBookEverySportAvailable() {
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
                assertNotNull("sportBuchungsVersuch", sportBuchungsVersuch);
                assertNotEquals("BUCHUNG_FEHLER", BUCHUNG_FEHLER, sportBuchungsVersuch.getStatus());
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