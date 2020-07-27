package de.chrisgw.sportbooking.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


@Data
@JsonFilter("lazyLoaderFilter")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportArt implements Comparable<SportArt> {

    private final String name;
    private final Semester semester;
    private final String url;

    @EqualsAndHashCode.Exclude
    private Set<SportAngebot> sportAngebote = new LinkedHashSet<>();


    @JsonCreator
    public SportArt(@JsonProperty("name") String name, @JsonProperty("semester") Semester semester,
            @JsonProperty("url") String url) {
        this.name = requireNonNull(name);
        this.semester = requireNonNull(semester);
        this.url = requireNonNull(url);
    }


    public Stream<SportAngebot> upcomingSportAngebote() {
        return sportAngebote.stream().filter(SportAngebot::isUpcoming);
    }


    public Optional<SportAngebot> findSportAngebot(String kursnummer) {
        return sportAngebote.stream().filter(sportAngebot -> sportAngebot.getKursnummer().equals(kursnummer)).findAny();
    }


    public void addSportAngebot(SportAngebot sportAngebot) {
        requireNonNull(sportAngebot).setSportArt(this);
        this.sportAngebote.add(sportAngebot);
    }

    public void addAllSportAngebot(Collection<SportAngebot> sportAngebote) {
        sportAngebote.forEach(this::addSportAngebot);
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
