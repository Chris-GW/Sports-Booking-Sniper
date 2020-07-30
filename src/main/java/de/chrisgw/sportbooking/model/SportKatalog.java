package de.chrisgw.sportbooking.model;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Data
public class SportKatalog implements Iterable<SportArt> {

    private String katalog;
    private LocalDate zeitraumStart;
    private LocalDate zeitraumEnde;
    private LocalDateTime abrufzeitpunkt = LocalDateTime.now();
    private Set<SportArt> sportArten = new TreeSet<>();


    public Optional<SportArt> findSportArtByName(String name) {
        return getSportArten().stream().filter(sportArt -> sportArt.getName().equalsIgnoreCase(name)).findFirst();
    }

    public boolean isInZeitraum(LocalDate localDate) {
        return !(localDate.isBefore(zeitraumStart) || localDate.isAfter(zeitraumEnde));
    }


    public boolean addSportArt(SportArt sportArt) {
        Objects.requireNonNull(sportArt).setSportKatalog(this);
        return sportArten.add(sportArt);
    }


    @Override
    public Iterator<SportArt> iterator() {
        return sportArten.iterator();
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(katalog).append(" (").append(abrufzeitpunkt).append(")\n");
        for (SportArt sportArt : sportArten) {
            str.append(sportArt).append("\n");
        }
        return str.deleteCharAt(str.length() - 1).toString();
    }
}
