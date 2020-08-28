package de.chrisgw.sportbookingsniper.buchung.steps;

import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.angebot.SportArt;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;


@Slf4j
public class GetSportAngebotWebpageSchritt extends SeleniumSportBuchungsSchritt {

    public GetSportAngebotWebpageSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return true;
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new SelectSportAngebotSchritt(driver), //
                new SelectEinzelPlatzSportAngebotSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        SportArt sportArt = sportAngebot.getSportArt();
        String sportArtUrl = sportArt.getUrl();
        log.debug("{}: WebDriver GET SportArt={} Angebot webpage URL={}", buchungsJob, sportAngebot, sportArtUrl);
        driver.get(sportArtUrl);
        if (log.isTraceEnabled()) {
            log.trace(Jsoup.parse(driver.getPageSource()).toString());
        }
        return super.executeBuchungsSchritt(buchungsJob);
    }

}
