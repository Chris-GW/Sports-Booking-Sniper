package de.chrisgw.sportsbookingsniper.buchung;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsStrategieImpl.defaultSportBuchungStrategie;


@Data
public class SportBuchungsJob {

    private int jobId;
    private SportTermin sportTermin;
    private String passwort;
    private List<Teilnehmer> teilnehmerListe = new ArrayList<>();
    private SportBuchungsStrategie buchungsStrategie = defaultSportBuchungStrategie();

    private boolean pausiert = false;
    private LocalDateTime buchungsBeginn;
    private LocalDateTime bevorstehenderBuchungsVersuch = buchungsStrategie.getNextTimeForCheck(this);
    private List<SportBuchungsVersuch> buchungsVersuche = new ArrayList<>();


    public SportBuchungsJob() {
        buchungsVersuche.add(SportBuchungsVersuch.newBuchungsVersuch(SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN));
    }


    public LocalDateTime getBevorstehenderBuchungsVersuch() {
        if (bevorstehenderBuchungsVersuch.isAfter(LocalDateTime.now())) {
            bevorstehenderBuchungsVersuch = buchungsStrategie.getNextTimeForCheck(this);
        }
        return bevorstehenderBuchungsVersuch;
    }


    public SportBuchungsVersuch lastSportBuchungsVersuch() {
        return buchungsVersuche.get(buchungsVersuche.size() - 1);
    }


    public SportBuchungsVersuchStatus getLastBuchungsVersuchStatus() {
        return lastSportBuchungsVersuch().getStatus();
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


    public SportAngebot getSportAngebot() {
        return sportTermin.getSportAngebot();
    }

    public SportArt getSportArt() {
        return getSportAngebot().getSportArt();
    }


    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        return String.format("(%d) %s", jobId, sportTermin.getName());
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
