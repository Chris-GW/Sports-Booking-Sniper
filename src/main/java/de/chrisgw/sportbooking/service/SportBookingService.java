package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.*;

import java.util.Set;


public interface SportBookingService {

    SportKatalog loadSportKatalog();

    Set<SportAngebot> fetchSportAngebote(SportArt sportArt);

    Set<SportTermin> fetchSportTermine(SportAngebot sportAngebot);

    SportBuchungsBestaetigung verbindlichBuchen(SportBuchungsJob sportBuchungsJob);

}
