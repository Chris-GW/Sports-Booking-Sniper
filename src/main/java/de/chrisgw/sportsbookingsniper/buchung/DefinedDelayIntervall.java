package de.chrisgw.sportsbookingsniper.buchung;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.util.Objects.requireNonNull;


@Data
public class DefinedDelayIntervall implements Comparable<DefinedDelayIntervall> {

    private final LocalTime from;
    private final LocalTime to;
    private final Duration delay;


    @JsonCreator(mode = Mode.PROPERTIES)
    public DefinedDelayIntervall(@JsonProperty("from") LocalTime from, @JsonProperty("to") LocalTime to,
            @JsonProperty("delay") Duration delay) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("expect startTime isBefore endTime");
        }
        if (delay.isNegative() || delay.toSeconds() < 60) {
            throw new IllegalArgumentException("expect positiv delay, which is longer than 60s, but was " + delay);
        }
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
        this.delay = requireNonNull(delay);
    }


    public boolean isInsideIntervall(LocalDateTime localDateTime) {
        return isInsideIntervall(localDateTime.toLocalTime());
    }

    public boolean isInsideIntervall(LocalTime localTime) {
        return !localTime.isBefore(from) && localTime.isBefore(to);
    }


    @Override
    public int compareTo(DefinedDelayIntervall other) {
        return new CompareToBuilder().append(this.getFrom(), other.getFrom())
                .append(this.getTo(), other.getTo())
                .toComparison();
    }

    @Override
    public String toString() {
        return from + " - " + to + " [" + delay + "]";
    }

}
