package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;


@Log4j2
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
        log.traceEntry("GetSportAngebotWebpageSchritt executeBuchungsSchritt( {} )", buchungsJob);
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        SportArt sportArt = sportAngebot.getSportArt();
        String sportArtUrl = sportArt.getUrl();
        log.debug("{}: WebDriver GET SportArt={} Angebot webpage URL={}", buchungsJob, sportAngebot, sportArtUrl);
        driver.get(sportArtUrl);
        log.trace(() -> Jsoup.parse(driver.getPageSource()).toString());
        return super.executeBuchungsSchritt(buchungsJob);
    }

}
