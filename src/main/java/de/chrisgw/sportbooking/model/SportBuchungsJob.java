package de.chrisgw.sportbooking.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import de.chrisgw.sportbooking.model.SportBuchungStrategieImpl.FixedPeriodTimeBuchungStrategie;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SportBuchungsJob implements Comparable<SportBuchungsJob> {

    private long jobId;
    private SportBuchungStrategie sportBuchungStrategie;

    private final SportTermin sportTermin;
    private final PersonenAngaben personenAngaben;


    public SportBuchungsJob(SportTermin sportTermin, PersonenAngaben personenAngaben) {
        this.sportTermin = Objects.requireNonNull(sportTermin);
        this.personenAngaben = Objects.requireNonNull(personenAngaben);
        setSportBuchungStrategie(new FixedPeriodTimeBuchungStrategie(15, TimeUnit.MINUTES));
    }


    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }


    public SportTermin getSportTermin() {
        return sportTermin;
    }

    public PersonenAngaben getPersonenAngaben() {
        return personenAngaben;
    }


    public SportBuchungStrategie getSportBuchungStrategie() {
        return sportBuchungStrategie;
    }

    public void setSportBuchungStrategie(SportBuchungStrategie sportBuchungStrategie) {
        this.sportBuchungStrategie = Objects.requireNonNull(sportBuchungStrategie);
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
