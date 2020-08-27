package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN;
import static de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_WARTELISTE;
import static de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch.newBuchungsVersuch;


@Slf4j
public class SelectSportAngebotSchritt extends SeleniumSportBuchungsSchritt {

    public SelectSportAngebotSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        return findAngebotBuchenCell(driver, sportAngebot).isPresent();
    }


    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new EnterPasswortForSportAngebotSchritt(driver), //
                new SelectSportTerminSchritt(driver), //
                new SubmitTeilnehmerAngabenSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        Optional<WebElement> angebotRow = findAngebotBuchenCell(driver, sportAngebot);
        Optional<WebElement> angebotBuchenBtn = angebotRow.flatMap(findElement(By.className("bs_btn_buchen")));
        if (angebotBuchenBtn.isPresent()) {
            String buchungsKursId = angebotBuchenBtn.get().getAttribute("name");
            log.debug("found SportAngebot input.bs_btn_buchen and click onto {}", buchungsKursId);
            angebotBuchenBtn.get().click();
            switchToNewOpenWindow();
            return super.executeBuchungsSchritt(buchungsJob);
        }
        Optional<WebElement> wartezeitElement = angebotRow.flatMap(findElement(By.className("bs_btn_autostart")));
        if (wartezeitElement.isPresent()) {
            String wartezeitText = wartezeitElement.get().getText();
            log.debug("couldn't find SportAngebot input.bs_btn_buchen {}", wartezeitElement.get());
            readBuchungsBeginn(wartezeitText).ifPresent(buchungsJob::setBuchungsBeginn);
            return newBuchungsVersuch(BUCHUNG_GESCHLOSSEN);
        }
        return newBuchungsVersuch(BUCHUNG_WARTELISTE);
    }

    private Optional<WebElement> findAngebotBuchenCell(SearchContext searchContext, SportAngebot sportAngebot) {
        String kursnummer = sportAngebot.getKursnummer();
        return searchContext.findElements(By.cssSelector("#bs_content table.bs_kurse tbody tr"))
                .stream()
                .filter(angebotRow -> {
                    WebElement kursnummerCell = angebotRow.findElement(By.className("bs_sknr"));
                    return kursnummer.equals(kursnummerCell.getAttribute("textContent"));
                })
                .findAny()
                .map(angebotRow -> angebotRow.findElement(By.className("bs_sbuch")));
    }

}
