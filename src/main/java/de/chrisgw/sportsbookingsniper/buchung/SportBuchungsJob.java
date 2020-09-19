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
import java.util.concurrent.*;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.KonfigurierbareSportBuchungsWiederholungStrategie.defaultKonfiguration;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;


@Data
public class SportBuchungsJob {

    private int jobId;
    private SportTermin sportTermin;
    private String passwort;
    private List<Teilnehmer> teilnehmerListe = new ArrayList<>();
    private SportBuchungsWiederholungStrategie buchungsWiederholungsStrategie = defaultKonfiguration();

    private boolean pausiert = false;
    private LocalDateTime buchungsBeginn;
    private LocalDateTime bevorstehenderBuchungsVersuch;
    private List<SportBuchungsVersuch> buchungsVersuche = new CopyOnWriteArrayList<>();


    public SportBuchungsJob() {
        buchungsVersuche.add(newBuchungsVersuch(SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN));
    }


    public boolean canContinue() {
        return !pausiert && getBuchungsVersuche() != null && lastSportBuchungsVersuch().getStatus()
                .canContineNextBuchungsVersuch();
    }


    public LocalDateTime getBevorstehenderBuchungsVersuch() {
        if (bevorstehenderBuchungsVersuch.isAfter(LocalDateTime.now())) {
            bevorstehenderBuchungsVersuch = buchungsWiederholungsStrategie.getNextTimeForCheck(this);
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
        return sportTermin.getName();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SportBuchungsJob))
            return false;

        SportBuchungsJob that = (SportBuchungsJob) o;
        return new EqualsBuilder().append(getJobId(), that.getJobId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getJobId()).toHashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

}
