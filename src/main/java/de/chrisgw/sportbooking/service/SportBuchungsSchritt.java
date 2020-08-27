package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;

import java.util.stream.Stream;


public interface SportBuchungsSchritt {

    boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob);

    SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob);

    Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte();

}
