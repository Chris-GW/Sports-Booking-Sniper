package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_WARTELISTE;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;


@Log4j2
public class SelectSportAngebotSchritt extends SeleniumSportBuchungsSchritt {

    public SelectSportAngebotSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        return findAngebotBuchenCell(sportAngebot).isPresent();
    }


    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new EnterPasswortForSportAngebotSchritt(driver), //
                new SelectSportTerminRadioOptionSchritt(driver), //
                new ClickSportTerminBuchenBtnSchritt(driver), //
                new SubmitTeilnehmerFormSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        Optional<WebElement> angebotRow = findAngebotBuchenCell(sportAngebot);
        Optional<WebElement> angebotBuchenBtn = angebotRow.flatMap(findElement(By.className("bs_btn_buchen")));
        if (angebotBuchenBtn.isPresent()) {
            String buchungsKursId = angebotBuchenBtn.get().getAttribute("name");
            log.debug("{}: click on found SportAngebot {} <input.bs_btn_buchen> for kursId={}", //
                    buchungsJob, sportAngebot, buchungsKursId);
            angebotBuchenBtn.get().click();
            switchToNewOpenWindow();
            return super.executeBuchungsSchritt(buchungsJob);
        }
        Optional<WebElement> buchungsBeginnSpan = angebotRow.flatMap(findElement(By.className("bs_btn_autostart")));
        if (buchungsBeginnSpan.isPresent()) {
            String buchungsBeginnText = buchungsBeginnSpan.get().getText();
            readBuchungsBeginn(buchungsBeginnText).ifPresent(buchungsJob::setBuchungsBeginn);
            log.info("{}: only found <input.bs_btn_autostart> for SportAngebot {} with text={}", //
                    buchungsJob, sportAngebot, buchungsBeginnText);
            return newBuchungsVersuch(BUCHUNG_GESCHLOSSEN);
        } else {
            log.info("{}: only Warteliste found for SportAngebot {}", buchungsJob, sportAngebot);
            return newBuchungsVersuch(BUCHUNG_WARTELISTE);
        }
    }

    private Optional<WebElement> findAngebotBuchenCell(SportAngebot sportAngebot) {
        String kursnummer = sportAngebot.getKursnummer();
        return driver.findElements(By.cssSelector("#bs_content table.bs_kurse tbody tr"))
                .stream()
                .filter(angebotRow -> {
                    WebElement kursnummerCell = angebotRow.findElement(By.className("bs_sknr"));
                    return kursnummer.equals(kursnummerCell.getAttribute("textContent"));
                })
                .findAny()
                .map(angebotRow -> angebotRow.findElement(By.className("bs_sbuch")));
    }

}
