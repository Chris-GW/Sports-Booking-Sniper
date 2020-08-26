package de.chrisgw.sportbooking.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportbooking.model.SportBuchungsStrategieImpl.defaultSportBuchungStrategie;


@Data
@NoArgsConstructor
public class SportBuchungsJob {

    private int jobId;
    private SportAngebot sportAngebot;
    private SportTermin sportTermin;
    private TeilnehmerAngaben teilnehmerAngaben;
    private String passwort;
    private SportBuchungsStrategie buchungsStrategie = defaultSportBuchungStrategie();

    private boolean pausiert = false;
    private LocalDateTime bevorstehenderBuchungsVersuch = buchungsStrategie.getNextTimeForCheck(this);
    private List<SportBuchungsVersuch> buchungsVersuche = new ArrayList<>();





    public LocalDateTime getBevorstehenderBuchungsVersuch() {
        if (bevorstehenderBuchungsVersuch.isAfter(LocalDateTime.now())) {
            bevorstehenderBuchungsVersuch = buchungsStrategie.getNextTimeForCheck(this);
        }
        return bevorstehenderBuchungsVersuch;
    }


    public SportBuchungsVersuch lastSportBuchungsVersuch() {
        return buchungsVersuche.get(buchungsVersuche.size() - 1);
    }

    public Duration durationTillNextCheck() {
        return Duration.between(getBevorstehenderBuchungsVersuch(), LocalDateTime.now());
    }


    public Stream<SportBuchungsVersuch> buchungsVersuche() {
        return buchungsVersuche.stream();
    }

    public int anzahlVersuche() {
        return buchungsVersuche.size();
    }


    public Optional<SportBuchungsBestaetigung> buchungsBestaetigung() {
        return Optional.ofNullable(lastSportBuchungsVersuch().getBuchungsBestaetigung());
    }


    public void addBuchungsVersuch(SportBuchungsVersuch buchungsVersuch) {
        buchungsVersuche.add(Objects.requireNonNull(buchungsVersuch));
    }

    public BigInteger getPreis() {
        return getSportAngebot().preisFor(teilnehmerAngaben.getTeilnehmerKategorie());
    }


    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        return String.format("(%d) %s", getJobId(), getSportTermin().getName());
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SportBuchungsJob that = (SportBuchungsJob) other;
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
