package de.chrisgw.sportbooking.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import static de.chrisgw.sportbooking.repository.HszRwthAachenSportKatalogRepository.DATE_TIME_FORMATTER;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportTermin implements Comparable<SportTermin> {

    private SportAngebot sportAngebot;

    private LocalDateTime buchungsBeginn;
    private LocalDateTime startZeit;
    private LocalDateTime endZeit;
    private boolean passwortGesichert = false;


    public boolean overlapseWith(SportTermin otherSportTermin) {
        LocalDateTime otherStartZeit = otherSportTermin.getStartZeit();
        LocalDateTime otherEndZeit = otherSportTermin.getEndZeit();
        return getStartZeit().isBefore(otherEndZeit) && getEndZeit().isAfter(otherStartZeit);
    }

    public boolean isBevorstehend() {
        return startZeit != null && LocalDateTime.now().isBefore(startZeit);
    }

    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        String sportName = getSportAngebot().getSportArt().getName();
        String kursnummer = getSportAngebot().getKursnummer();
        String dayOfWeekStr = getStartZeit().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        String start = DATE_TIME_FORMATTER.format(getStartZeit());
        String end = ISO_LOCAL_TIME.format(getEndZeit());
        return String.format("%s (%s) von %s %s bis %s", sportName, kursnummer, dayOfWeekStr, start, end);
    }


    @JsonIgnore
    public LocalDate getTerminDate() {
        return getStartZeit().toLocalDate();
    }

    @JsonIgnore
    public Duration getDuration() {
        return Duration.between(getStartZeit(), getEndZeit());
    }


    @Override
    public int compareTo(SportTermin otherTermin) {
        return new CompareToBuilder().append(this.getSportAngebot(), otherTermin.getSportAngebot())
                .append(this.getStartZeit(), otherTermin.getStartZeit())
                .append(this.getEndZeit(), otherTermin.getEndZeit())
                .toComparison();
    }

    @Override
    public String toString() {
        return getName();
    }

}
