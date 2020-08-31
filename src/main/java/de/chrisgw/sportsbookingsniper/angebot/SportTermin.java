package de.chrisgw.sportsbookingsniper.angebot;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.TIME_FORMATTER;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportTermin implements Comparable<SportTermin> {

    private SportAngebot sportAngebot;

    private LocalDateTime startZeit;
    private LocalDateTime endZeit;


    public boolean overlapseWith(SportTermin otherSportTermin) {
        LocalDateTime otherStartZeit = otherSportTermin.getStartZeit();
        LocalDateTime otherEndZeit = otherSportTermin.getEndZeit();
        return getStartZeit().isBefore(otherEndZeit) && getEndZeit().isAfter(otherStartZeit);
    }

    public boolean isSameTimeSlotAs(SportTermin sportTermin) {
        return startZeit.isEqual(sportTermin.getStartZeit()) && endZeit.isEqual(sportTermin.getEndZeit());
    }

    public boolean isBevorstehend() {
        return startZeit != null && LocalDateTime.now().isBefore(startZeit);
    }

    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        String sportName = getSportAngebot().getSportArt().getName();
        String kursnummer = getSportAngebot().getKursnummer();
        return String.format("%s (%s) von %s ", sportName, kursnummer, formatTerminZeitraum());
    }

    @JsonIgnore
    public String formatTerminZeitraum() {
        DateTimeFormatter terminStartFormatter = DateTimeFormatter.ofPattern("ccc dd.MM. HH:mm");
        String terminStartStr = terminStartFormatter.format(startZeit);
        String endZeitStr = TIME_FORMATTER.format(endZeit);
        return terminStartStr + "-" + endZeitStr;
    }


    @JsonIgnore
    public LocalDate getTerminDate() {
        return getStartZeit().toLocalDate();
    }

    @JsonIgnore
    public DayOfWeek getDayOfWeek() {
        return startZeit.getDayOfWeek();
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
