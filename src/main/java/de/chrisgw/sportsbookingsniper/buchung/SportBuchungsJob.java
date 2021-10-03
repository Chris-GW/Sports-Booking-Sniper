package de.chrisgw.sportsbookingsniper.buchung;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.buchung.strategie.SportBuchungsStrategie;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_GESCHLOSSEN;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newBuchungsVersuch;
import static de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie.defaultKonfiguration;


@Data
public class SportBuchungsJob {

    private int jobId;

    private SportAngebot sportAngebot;
    private SportTermin sportTermin;
    private String passwort;
    private List<Teilnehmer> teilnehmerListe = new ArrayList<>();
    private SportBuchungsStrategie buchungsWiederholungsStrategie = defaultKonfiguration();

    private boolean pausiert = false;
    private LocalDateTime buchungsBeginn;
    private LocalDateTime bevorstehenderBuchungsVersuch;
    private List<SportBuchungsVersuch> buchungsVersuche = new CopyOnWriteArrayList<>();


    public SportBuchungsJob() {
        addBuchungsVersuch(newBuchungsVersuch(BUCHUNG_GESCHLOSSEN));
    }


    public boolean canContinue() {
        return !pausiert && getLastBuchungsVersuchStatus().canContinueNextBuchungsVersuch();
    }


    public SportBuchungsVersuch lastSportBuchungsVersuch() {
        return buchungsVersuche.get(buchungsVersuche.size() - 1);
    }


    public SportBuchungsVersuchStatus getLastBuchungsVersuchStatus() {
        return lastSportBuchungsVersuch().getStatus();
    }

    public Duration durationTillNextCheck() {
        return Duration.between(LocalDateTime.now(), getBevorstehenderBuchungsVersuch());
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
        if (buchungsVersuch.getStatus().canContinueNextBuchungsVersuch()) {
            bevorstehenderBuchungsVersuch = buchungsWiederholungsStrategie.getNextTimeForCheck(this);
        }
    }


    public void setBuchungsWiederholungsStrategie(SportBuchungsStrategie buchungsWiederholungsStrategie) {
        if (buchungsWiederholungsStrategie == null) {
            buchungsWiederholungsStrategie = defaultKonfiguration();
        }
        this.buchungsWiederholungsStrategie = buchungsWiederholungsStrategie;
        this.bevorstehenderBuchungsVersuch = buchungsWiederholungsStrategie.getNextTimeForCheck(this);
    }


    public SportArt getSportArt() {
        return getSportAngebot().getSportArt();
    }


    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        String sportName = getSportAngebot().getSportArt().getName();
        String kursnummer = getSportAngebot().getKursnummer();
        String terminZeitraum = sportTermin.formatTerminZeitraum();
        return String.format("%s (%s) von %s ", sportName, kursnummer, terminZeitraum);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SportBuchungsJob)) {
            return false;
        }
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
