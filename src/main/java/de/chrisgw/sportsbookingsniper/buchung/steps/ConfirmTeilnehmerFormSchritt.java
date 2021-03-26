package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newErfolgreicherBuchungsVersuch;


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
        if (buchungsJob.getJobId() == -1) {
            for (int i = 0; !trySubmitTillStaleness(verbindlichBuchenBtn); i++) {
                System.out.println(i + " submit");
            }
            return super.executeBuchungsSchritt(buchungsJob);
        } else {
            // TODO remove dummy SportBuchungsBestaetigung
            SportBuchungsBestaetigung buchungsBestaetigung = new SportBuchungsBestaetigung();
            buchungsBestaetigung.setBuchungsJob(buchungsJob);
            buchungsBestaetigung.setBuchungsNummer("12345");
            buchungsBestaetigung.setBuchungsBestaetigungUrl("http://egal.de/test12345");
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
