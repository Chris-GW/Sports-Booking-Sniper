package de.chrisgw.sportbooking.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import de.chrisgw.sportbooking.model.SportBuchungStrategieImpl.FixedPeriodTimeBuchungStrategie;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;


@Data
public class SportBuchungsJob implements Comparable<SportBuchungsJob> {

    private long jobId;
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonIgnore
    private SportBuchungStrategie sportBuchungStrategie;

    private SportTermin sportTermin;
    private PersonenAngaben personenAngaben;


    public SportBuchungsJob() {
        setSportBuchungStrategie(new FixedPeriodTimeBuchungStrategie(15, TimeUnit.MINUTES));
    }

    public SportBuchungsJob(SportTermin sportTermin, PersonenAngaben personenAngaben) {
        this.sportTermin = requireNonNull(sportTermin);
        this.personenAngaben = requireNonNull(personenAngaben);
        setSportBuchungStrategie(new FixedPeriodTimeBuchungStrategie(15, TimeUnit.MINUTES));
    }


    public LocalDateTime getNextTimeForCheckTermin() {
        return sportBuchungStrategie.getNextTimeForCheckTermin(this);
    }


    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        return String.format("(%d) %s", getJobId(), getSportTermin().getName());
    }


    @Override
    public int compareTo(SportBuchungsJob o) {
        return new CompareToBuilder().append(this.getSportTermin(), o.getSportTermin())
                .append(this.getJobId(), o.getJobId())
                .toComparison();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        SportBuchungsJob that = (SportBuchungsJob) o;
        return new EqualsBuilder().append(jobId, that.jobId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(jobId).toHashCode();
    }


    @Override
    public String toString() {
        return getName();
    }

}
