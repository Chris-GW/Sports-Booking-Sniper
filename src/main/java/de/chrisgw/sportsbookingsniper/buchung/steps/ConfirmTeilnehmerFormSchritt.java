package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        WebElement weiterZurBuchungBtn = findFormSubmitBtn().orElseThrow(RuntimeException::new);
        if (buchungsJob.getJobId() == -1) { // TODO always submit
            weiterZurBuchungBtn.submit();
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

}
