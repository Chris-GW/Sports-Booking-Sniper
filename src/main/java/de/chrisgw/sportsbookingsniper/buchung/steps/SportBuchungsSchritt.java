package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;

import java.util.stream.Stream;


public interface SportBuchungsSchritt {

    boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob);

    SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob);

    Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob);

}
