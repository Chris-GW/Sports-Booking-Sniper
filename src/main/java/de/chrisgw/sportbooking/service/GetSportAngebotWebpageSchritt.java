package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportArt;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;


public class GetSportAngebotWebpageSchritt extends SeleniumSportBuchungsSchritt {

    public GetSportAngebotWebpageSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return true;
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new SelectSportAngebotSchritt(driver), //
                new SelectEinzelPlatzSportAngebotSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        SportArt sportArt = sportAngebot.getSportArt();
        driver.get(sportArt.getUrl());
        return super.executeBuchungsSchritt(buchungsJob);
    }

}
