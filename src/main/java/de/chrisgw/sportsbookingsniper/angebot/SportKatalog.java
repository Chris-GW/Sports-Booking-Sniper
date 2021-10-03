package de.chrisgw.sportsbookingsniper.angebot;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Data
@Builder
@JsonDeserialize(builder = SportKatalog.SportKatalogBuilder.class)
public class SportKatalog implements Iterable<SportArt> {

    private final String katalog;
    private final LocalDate zeitraumStart;
    private final LocalDate zeitraumEnde;
    @Builder.Default
    private final LocalDateTime abrufzeitpunkt = LocalDateTime.now();

    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private final Set<SportArt> sportArten = new TreeSet<>();


    public Optional<SportArt> findSportArtByName(String name) {
        return getSportArten().stream().filter(sportArt -> sportArt.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<SportAngebot> findSportAngebot(SportAngebot sportAngebot) {
        return getSportArten().stream()
                .filter(sportAngebot.getSportArt()::equals)
                .map(SportArt::getSportAngebote)
                .flatMap(Collection::stream)
                .filter(sportAngebot::hasEqualKursnummer)
                .findAny();
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
