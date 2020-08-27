package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;


public class EnterPasswortForSportAngebotSchritt extends SeleniumSportBuchungsSchritt {

    public EnterPasswortForSportAngebotSchritt(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        // TODO EnterSportAngebotPasswort
        return false;
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new SelectSportTerminSchritt(driver), new SubmitTeilnehmerAngabenSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        String passwort = buchungsJob.getPasswort();
        driver.findElement(By.tagName("input")).sendKeys(passwort);
        return super.executeBuchungsSchritt(buchungsJob);
    }

}
