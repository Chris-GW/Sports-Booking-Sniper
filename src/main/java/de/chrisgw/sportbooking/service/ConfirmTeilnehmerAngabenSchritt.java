package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Stream;


public class ConfirmTeilnehmerAngabenSchritt extends SeleniumSportBuchungsSchritt {

    public ConfirmTeilnehmerAngabenSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return true;
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new SportBuchungBestaetigungSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        WebElement bsForm = driver.findElement(By.name("bsform"));
        WebElement bsFormFooter = bsForm.findElement(By.id("bs_foot"));
        List<WebElement> bsFormButtons = bsFormFooter.findElements(By.tagName("input"));
        WebElement weiterZurBuchungBtn = bsFormButtons.stream()
                .filter(this::isVerbindlichBuchenFormBtn)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No 'weiter zur Buchung' submit Button found"));
        weiterZurBuchungBtn.submit();
        return super.executeBuchungsSchritt(buchungsJob);
    }

    private boolean isVerbindlichBuchenFormBtn(WebElement bsFormButton) {
        String value = bsFormButton.getAttribute("value");
        return "verbindlich buchen".equalsIgnoreCase(value) || "kostenpflichtig buchen".equalsIgnoreCase(value);
    }

}
