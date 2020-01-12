package de.chrisgw.sportbooking.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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

import static de.chrisgw.sportbooking.service.AachenSportBookingService.DATE_TIME_FORMATTER;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportTermin implements Comparable<SportTermin> {

    private SportAngebot sportAngebot;

    private LocalDateTime buchungsBeginn;
    private LocalDateTime startZeit;
    private LocalDateTime endZeit;
    private SportTerminStatus status = SportTerminStatus.WARTELISTE;


    public boolean hasSameStartAndEndDateTime(LocalDateTime startZeit, LocalDateTime endZeit) {
        return this.getStartZeit().isEqual(startZeit) && this.getEndZeit().isEqual(endZeit);
    }

    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        String sportName = getSportAngebot().getSportArt().getName();
        String kursnummer = getSportAngebot().getKursnummer();
        String dayOfWeekStr = startZeit.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        String start = DATE_TIME_FORMATTER.format(startZeit);
        String end = ISO_LOCAL_TIME.format(endZeit);
        return String.format("%s (%s) von %s %s bis %s", sportName, kursnummer, dayOfWeekStr, start, end);
    }

    public SportTerminStatus getStatus() {
        if (LocalDateTime.now().isAfter(this.getStartZeit())) {
            return SportTerminStatus.ABGELAUFEN;
        }
        return status;
    }


    public LocalDate getTerminDate() {
        return startZeit.toLocalDate();
    }

    public Duration getDuration() {
        return Duration.between(startZeit, endZeit);
    }


    public boolean hasStatus(SportTerminStatus terminStatus) {
        return this.getStatus().equals(terminStatus);
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

    public enum SportTerminStatus {
        OFFEN, WARTELISTE, GESCHLOSSEN, ABGELAUFEN
    }

}
