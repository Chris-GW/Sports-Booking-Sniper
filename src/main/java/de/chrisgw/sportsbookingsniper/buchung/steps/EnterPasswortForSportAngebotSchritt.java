package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLER;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;


@Slf4j
public class EnterPasswortForSportAngebotSchritt extends SeleniumSportBuchungsSchritt {

    public EnterPasswortForSportAngebotSchritt(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> bsFormMain = findElement(driver, By.id("bs_form_main"));
        Optional<WebElement> passwdInput = bsFormMain.flatMap(findElement(By.name("passwd")));
        return passwdInput.isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new SelectSportTerminRadioOptionSchritt(driver), new SubmitTeilnehmerFormSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        String passwort = buchungsJob.getPasswort();
        WebElement bsFormMain = driver.findElement(By.id("bs_form_main"));
        WebElement passwdInput = bsFormMain.findElement(By.name("passwd"));
        log.debug("{}: enter passwort=={}", buchungsJob, passwort);
        passwdInput.clear();
        passwdInput.sendKeys(passwort);

        WebElement weiterBtn = findFormSubmitBtn("weiter").orElseThrow(RuntimeException::new);
        weiterBtn.click();
        if (isNextBuchungsSchritt(buchungsJob)) {
            log.error("{}: entered password={} was wrong", buchungsJob, passwort);
            return newBuchungsVersuch(BUCHUNG_FEHLER);
        }
        return super.executeBuchungsSchritt(buchungsJob);
    }

}
