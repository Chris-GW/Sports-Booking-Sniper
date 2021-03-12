package de.chrisgw.sportsbookingsniper.angebot;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.TIME_FORMATTER;
import static java.util.Objects.requireNonNull;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportTermin implements Comparable<SportTermin> {

    private final LocalDateTime startZeit;
    private final LocalDateTime endZeit;


    public SportTermin(LocalDateTime startZeit, LocalDateTime endZeit) {
        this.startZeit = requireNonNull(startZeit);
        this.endZeit = requireNonNull(endZeit);
        if (endZeit.isBefore(startZeit)) {
            throw new IllegalArgumentException("startZeit needs to be before endZeit");
        }
    }


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
        return new CompareToBuilder().append(this.getStartZeit(), otherTermin.getStartZeit())
                .append(this.getEndZeit(), otherTermin.getEndZeit())
                .toComparison();
    }

    @Override
    public String toString() {
        return formatTerminZeitraum();
    }

}
