package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Stream;


@Slf4j
public class SelectSportTerminRadioOptionSchritt extends SeleniumSportBuchungsSchritt {

    public SelectSportTerminRadioOptionSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return false; // TODO SelectSportTerminRadioOptionSchritt
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new SubmitTeilnehmerFormSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {

        submitTerminSelection(buchungsJob);
        return null;
    }

    private void submitTerminSelection(SportBuchungsJob buchungsJob) {
        List<WebElement> footerSubmitBtns = driver.findElement(By.id("bs_foot")).findElements(By.tagName("input"));
        WebElement submitBtn = footerSubmitBtns.stream()
                .filter(webElement -> webElement.getAttribute("type").equalsIgnoreCase("submit"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not find expected from submit btn"));
        log.debug("{}: click on found SportTermin select form submitBtn", buchungsJob);
        submitBtn.click();
    }

}
