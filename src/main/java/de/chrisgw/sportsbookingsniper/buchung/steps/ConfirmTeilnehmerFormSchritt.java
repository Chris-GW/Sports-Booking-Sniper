package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        WebElement verbindlichBuchenBtn = findFormSubmitBtn().orElseThrow(() -> new IllegalStateException(
                "No 'verbindlich buchen' or 'kostenpflichtig buchen' submit Button found"));
        // TODO remove if and always submit
        if (buchungsJob.getJobId() == Integer.MAX_VALUE) {
            for (int i = 0; !trySubmitTillStaleness(verbindlichBuchenBtn); i++) {
                log.trace(i + " submit");
            }
            return super.executeBuchungsSchritt(buchungsJob);
        } else {
            // TODO remove dummy SportBuchungsBestaetigung
            SportBuchungsBestaetigung buchungsBestaetigung = new SportBuchungsBestaetigung();
            buchungsBestaetigung.setBuchungsJob(buchungsJob);
            buchungsBestaetigung.setBuchungsNummer("12345");
            buchungsBestaetigung.setBuchungsBestaetigungUrl("https://example.com/test12345");
            return newErfolgreicherBuchungsVersuch(buchungsBestaetigung);
        }
    }

    private Optional<WebElement> findFormSubmitBtn() {
        return findFormSubmitBtn("verbindlich buchen", "kostenpflichtig buchen");
    }


    private boolean trySubmitTillStaleness(WebElement verbindlichBuchenBtn) {
        try {
            verbindlichBuchenBtn.submit();
            return new WebDriverWait(driver, 1).until(ExpectedConditions.stalenessOf(verbindlichBuchenBtn));
        } catch (TimeoutException e) {
            return false;
        }
    }

}
