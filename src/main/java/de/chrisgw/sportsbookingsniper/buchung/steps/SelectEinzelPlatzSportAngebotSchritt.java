package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_WARTELISTE;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.TIME_FORMATTER;


@Slf4j
public class SelectEinzelPlatzSportAngebotSchritt extends SeleniumSportBuchungsSchritt {

    public SelectEinzelPlatzSportAngebotSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        List<WebElement> platzBuchenTables = driver.findElements(By.cssSelector("#bs_content table.bs_platz"));
        return !platzBuchenTables.isEmpty();
    }


    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new EnterPasswortForSportAngebotSchritt(driver), //
                new SelectSportTerminRadioOptionSchritt(driver), //
                new ClickSportTerminBuchenBtnSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        List<WebElement> platzBuchenCells = findPlatzBuchenCells(driver, buchungsJob);
        Optional<WebElement> openPlatzBuchenBtn = platzBuchenCells.stream()
                .map(findElement(By.className("bs_btn_buchen")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
        if (openPlatzBuchenBtn.isPresent()) {
            String buchungsKursId = openPlatzBuchenBtn.get().getAttribute("name");
            log.debug("found SportAngebot .bs_btn_buchen and click onto {}", buchungsKursId);
            openPlatzBuchenBtn.get().click();
            switchToNewOpenWindow();
            return super.executeBuchungsSchritt(buchungsJob);
        }

        Optional<WebElement> buchungsBeginnSpan = platzBuchenCells.stream()
                .map(findElement(By.className("bs_btn_autostart")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
        if (buchungsBeginnSpan.isPresent()) {
            String buchungsBeginnText = buchungsBeginnSpan.get().getText();
            log.debug("couldn't find SportAngebot .bs_btn_buchen, but buchungsBeginn {}", buchungsBeginnSpan.get());
            readBuchungsBeginn(buchungsBeginnText).ifPresent(buchungsJob::setBuchungsBeginn);
            return newBuchungsVersuch(BUCHUNG_GESCHLOSSEN);
        }
        return newBuchungsVersuch(BUCHUNG_WARTELISTE);
    }


    private List<WebElement> findPlatzBuchenCells(SearchContext searchContext, SportBuchungsJob buchungsJob) {
        SportTermin sportTermin = buchungsJob.getSportTermin();
        String wochentagStr = sportTermin.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY);
        String startZeitStr = TIME_FORMATTER.format(sportTermin.getStartZeit());
        String endZeitStr = TIME_FORMATTER.format(sportTermin.getEndZeit());
        // Donnerstag 20:30-21:30
        String neededZeitpunkt = wochentagStr + " " + startZeitStr + "-" + endZeitStr;
        return searchContext.findElements(By.cssSelector("#bs_content table.bs_platz td.bs_sbuch"))
                .stream()
                .filter(platzBuchungsCell -> {
                    String zeitpunktStr = platzBuchungsCell.getAttribute("title");
                    return neededZeitpunkt.equals(zeitpunktStr);
                })
                .collect(Collectors.toList());
    }

}
