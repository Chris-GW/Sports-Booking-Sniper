package de.chrisgw.sportbooking.model;


import java.time.LocalDateTime;
import java.util.*;


public class SportKatalog {

    private LocalDateTime uhrzeitAberufen = LocalDateTime.now();

    private Set<SportArt> sportArten = new TreeSet<>();


    public SportKatalog() {

    }


    public Optional<SportArt> findSportArtByName(String name) {
        return getSportArten().stream().filter(sportArt -> sportArt.getName().equalsIgnoreCase(name)).findFirst();
    }


    public Set<SportArt> getSportArten() {
        return sportArten;
    }

    public void setSportArten(Set<SportArt> sportArten) {
        this.sportArten = Objects.requireNonNull(sportArten);
    }

    public void addSportArt(SportArt sportArt) {
        this.sportArten.add(Objects.requireNonNull(sportArt));
    }

    public void addAllSportArten(Collection<? extends SportArt> collection) {
        collection.forEach(this::addSportArt);
    }


    public LocalDateTime getUhrzeitAberufen() {
        return uhrzeitAberufen;
    }

    public void setUhrzeitAberufen(LocalDateTime uhrzeitAberufen) {
        this.uhrzeitAberufen = uhrzeitAberufen;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("SportKatalog von: ").append(uhrzeitAberufen).append("\n");
        for (SportArt sportArt : sportArten) {
            str.append(sportArt).append("\n");
        }
        return str.deleteCharAt(str.length() - 1).toString();
    }

}