package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.SportBookingSniperApplication;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newErfolgreicherBuchungsVersuch;


@Log4j2
public class ConfirmTeilnehmerFormSchritt extends SeleniumSportBuchungsSchritt {


    public ConfirmTeilnehmerFormSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> formSubmitBtn = findFormSubmitBtn();
        return formSubmitBtn.isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new SportBuchungBestaetigungSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        if (!SportBookingSniperApplication.finalBooking.get()) {
            return dummyBuchungsBestaetigung(buchungsJob);
        }
        WebElement verbindlichBuchenBtn = findFormSubmitBtn().orElseThrow(() -> new IllegalStateException(
                "No 'verbindlich buchen' or 'kostenpflichtig buchen' submit Button found"));
        for (int i = 0; !trySubmitTillStaleness(verbindlichBuchenBtn); i++) {
            log.trace(i + " submit");
        }
        return super.executeBuchungsSchritt(buchungsJob);
    }

    private Optional<WebElement> findFormSubmitBtn() {
        return findFormSubmitBtn("verbindlich buchen", "kostenpflichtig buchen");
    }


    private boolean trySubmitTillStaleness(WebElement verbindlichBuchenBtn) {
        try {
            verbindlichBuchenBtn.submit();
            return new WebDriverWait(driver, Duration.ofMillis(1000))
                    .until(ExpectedConditions.stalenessOf(verbindlichBuchenBtn));
        } catch (TimeoutException e) {
            return false;
        }
    }


    private SportBuchungsVersuch dummyBuchungsBestaetigung(SportBuchungsJob buchungsJob) {
        int jobId = buchungsJob.getJobId();
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        String buchungsNummer = sportAngebot.getKursnummer() + "-" + jobId;
        String buchungsBestaetigungUrl = sportAngebot.getKursinfoUrl() + "/bestaetigung_" + jobId;
        var buchungsBestaetigung = new SportBuchungsBestaetigung();
        buchungsBestaetigung.setBuchungsJob(buchungsJob);
        buchungsBestaetigung.setBuchungsNummer(buchungsNummer);
        buchungsBestaetigung.setBuchungsBestaetigungUrl(buchungsBestaetigungUrl);
        return newErfolgreicherBuchungsVersuch(buchungsBestaetigung);
    }

}
