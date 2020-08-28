package de.chrisgw.sportbookingsniper.buchung.steps;

import de.chrisgw.sportbookingsniper.angebot.SportTermin;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class SelectSportTerminSchritt extends SeleniumSportBuchungsSchritt {

    public SelectSportTerminSchritt(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return SeleniumSportBuchungsSchritt.findElement(driver, By.className("bs_etvg")).isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new SubmitTeilnehmerAngabenSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        WebElement terminInput = findTerminInput(driver, buchungsJob).orElseThrow(RuntimeException::new);
        terminInput.click();
        List<WebElement> footerSubmitBtns = driver.findElement(By.id("bs_foot")).findElements(By.tagName("input"));
        WebElement submitBtn = footerSubmitBtns.stream()
                .filter(webElement -> webElement.getAttribute("type").equalsIgnoreCase("submit"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not find submit btn"));
        submitBtn.click();
        return super.executeBuchungsSchritt(buchungsJob);
    }


    private Optional<WebElement> findTerminInput(WebDriver driver, SportBuchungsJob buchungsJob) {
        SportTermin sportTermin = buchungsJob.getSportTermin();
        String formattedTerminDate = DateTimeFormatter.ISO_DATE.format(sportTermin.getTerminDate());
        WebElement mainBuchungsForm = driver.findElement(By.className("bs_etvg"));
        for (WebElement terminInput : mainBuchungsForm.findElements(By.tagName("input"))) {
            String name = terminInput.getAttribute("name");
            String value = terminInput.getAttribute("value");
            if (name.equalsIgnoreCase("Termin") && value.equals(formattedTerminDate)) {
                return Optional.of(terminInput);
            }
        }
        return Optional.empty();
    }


}
