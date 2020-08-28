package de.chrisgw.sportbookingsniper.buchung.steps;

import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;

import java.util.stream.Stream;


public interface SportBuchungsSchritt {

    boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob);

    SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob);

    Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte();

}
