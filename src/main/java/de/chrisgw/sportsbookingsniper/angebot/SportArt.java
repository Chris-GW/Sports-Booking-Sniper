package de.chrisgw.sportsbookingsniper.angebot;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


@Data
public class SportArt implements Comparable<SportArt> {

    private SportKatalog sportKatalog;
    private final String name;
    private final String url;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Set<SportAngebot> sportAngebote = new LinkedHashSet<>();



    @JsonCreator
    public SportArt(@JsonProperty("name") String name, @JsonProperty("url") String url) {
        this.name = requireNonNull(name);
        this.url = requireNonNull(url);
    }


    public Stream<SportAngebot> upcomingSportAngebote() {
        return sportAngebote.stream().filter(SportAngebot::isBevorstehend);
    }


    public Optional<SportAngebot> findSportAngebot(String kursnummer) {
        return sportAngebote.stream().filter(sportAngebot -> sportAngebot.getKursnummer().equals(kursnummer)).findAny();
    }


    public void addSportAngebot(SportAngebot sportAngebot) {
        requireNonNull(sportAngebot).setSportArt(this);
        this.sportAngebote.add(sportAngebot);
    }


    @Override
    public int compareTo(SportArt otherSportArt) {
        return this.getName().compareTo(otherSportArt.getName());
    }

    @Override
    public String toString() {
        return getName();
    }

}
