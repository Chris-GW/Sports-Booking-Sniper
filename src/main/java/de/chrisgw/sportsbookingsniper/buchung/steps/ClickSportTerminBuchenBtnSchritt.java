package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.DATE_FORMATTER;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN;


@Slf4j
public class ClickSportTerminBuchenBtnSchritt extends SeleniumSportBuchungsSchritt {

    public ClickSportTerminBuchenBtnSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> bs_etvgElement = findElement(driver, By.className("bs_etvg"));
        // TODO differenciate between radio termin option and buchen btn
        return bs_etvgElement.isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new SubmitTeilnehmerFormSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> terminInput = findTerminBuchenBtn(buchungsJob);
        if (!terminInput.isPresent()) {
            log.info("{}: couldn't find select option for SportTermin {}", buchungsJob, buchungsJob.getSportTermin());
            return SportBuchungsVersuch.newBuchungsVersuch(BUCHUNG_GESCHLOSSEN);
        }
        log.debug("{}: click on termin select option for SportTermin {}", buchungsJob, buchungsJob.getSportTermin());
        terminInput.get().click();
        return super.executeBuchungsSchritt(buchungsJob);
    }


    private Optional<WebElement> findTerminBuchenBtn(SportBuchungsJob buchungsJob) {
        SportTermin sportTermin = buchungsJob.getSportTermin();
        List<WebElement> terminRows = driver.findElements(By.cssSelector(".bs_etvg label"));
        return terminRows.stream()
                .filter(isTerminRowFor(sportTermin))
                .findAny()
                .flatMap(findElement(By.tagName("input")));
    }

    private Predicate<WebElement> isTerminRowFor(SportTermin sportTermin) {
        return terminRow -> {
            Optional<WebElement> terminDateDiv = findElement(terminRow, By.className("pointer"));
            return terminDateDiv.map(WebElement::getText)
                    .map(terminDateStr -> LocalDate.parse(terminDateStr, DATE_FORMATTER))
                    .map(sportTermin.getTerminDate()::isEqual)
                    .orElse(false);
        };
    }


}
