package de.chrisgw.sportsbookingsniper.angebot;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.UNBEKANNTE_BUCHUNGSART;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportAngebot implements Comparable<SportAngebot> {

    private SportArt sportArt;

    private String kursnummer;
    private String details;
    private String ort;
    private String leitung;
    private SportAngebotPreis preis = new SportAngebotPreis();

    private String kursinfoUrl;
    private SportAngebotBuchungsArt buchungsArt = UNBEKANNTE_BUCHUNGSART;
    private boolean passwortGesichert = false;
    private LocalDate zeitraumStart;
    private LocalDate zeitraumEnde;

    @EqualsAndHashCode.Exclude
    private SortedSet<SportTermin> sportTermine = new TreeSet<>();


    public boolean isBevorstehend() {
        return !LocalDate.now().isAfter(zeitraumEnde);
    }

    public boolean isAktuell() {
        LocalDate now = LocalDate.now();
        return !(now.isBefore(zeitraumStart) || now.isAfter(zeitraumEnde));
    }


    public boolean isPaymentRequierd() {
        return preis.isPaymentRequierd();
    }

    public BigInteger preisFor(TeilnehmerKategorie teilnehmerKategorie) {
        return preis.preisFor(teilnehmerKategorie);
    }

    public boolean hasEqualKursnummer(SportAngebot otherSportAngebot) {
        return this.getKursnummer().equals(otherSportAngebot.getKursnummer());
    }


    public Optional<SportTermin> findByDate(LocalDate terminDate) {
        return sportTermine().filter(sportTermin -> sportTermin.getTerminDate().equals(terminDate)).findFirst();
    }


    @JsonIgnore
    public Stream<SportTermin> bevorstehendeSportTermine() {
        return sportTermine().filter(SportTermin::isBevorstehend).sorted();
    }

    @JsonIgnore
    public Stream<SportTermin> sportTermine() {
        return sportTermine.stream();
    }


    public void addSportTermin(SportTermin sportTermin) {
        this.sportTermine.add(sportTermin);
    }


    @JsonProperty(access = Access.READ_ONLY)
    public String getName() {
        String kursnummer = getKursnummer();
        String sportName = getSportArt().getName();
        return String.format("(%s) %s", kursnummer, sportName);
    }


    @Override
    public int compareTo(SportAngebot otherAngebot) {
        return new CompareToBuilder().append(this.getSportArt(), otherAngebot.getSportArt())
                .append(this.getKursnummer(), otherAngebot.getKursnummer())
                .toComparison();
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", getKursnummer(), getDetails());
    }


    public enum SportAngebotBuchungsArt {

        UNBEKANNTE_BUCHUNGSART, //
        ANGEBOT_TICKET_BUCHUNG, //
        EINZEL_TERMIN_BUCHUNG, //
        EINZEL_PLATZ_BUCHUNG; //

    }

}
