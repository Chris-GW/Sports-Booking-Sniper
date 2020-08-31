package de.chrisgw.sportsbookingsniper.angebot;

import java.util.Set;


public interface SportKatalogRepository {

    SportKatalog findCurrentSportKatalog();

    Set<SportAngebot> findSportAngeboteFor(SportArt sportArt);

    Set<SportTermin> findSportTermineFor(SportAngebot sportAngebot);

}
