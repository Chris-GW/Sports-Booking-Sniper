package de.chrisgw.sportbooking.repository;

import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportArt;
import de.chrisgw.sportbooking.model.SportKatalog;
import de.chrisgw.sportbooking.model.SportTermin;

import java.util.Set;


public interface SportKatalogRepository {

    SportKatalog findCurrentSportKatalog();

    Set<SportAngebot> findSportAngeboteFor(SportArt sportArt);

    Set<SportTermin> findSportTermineFor(SportAngebot sportAngebot);

}
