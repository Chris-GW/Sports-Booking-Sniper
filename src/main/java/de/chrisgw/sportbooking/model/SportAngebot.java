package de.chrisgw.sportbooking.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class SportAngebot implements Comparable<SportAngebot> {

    private SportArt sportArt;

    private String kursnummer;
    private String details;
    private String ort;
    private String leitung;
    private boolean einzelterminBuchung;
    private SportAngebotPreis preis = new SportAngebotPreis();
    private String kursinfoUrl;
    private LocalDate zeitraumStart;
    private LocalDate zeitraumEnde;

    @EqualsAndHashCode.Exclude
    private Set<SportTermin> sportTermine = new LinkedHashSet<>();


    public boolean isUpcoming() {
        return !LocalDate.now().isAfter(zeitraumEnde);
    }

    public boolean isAktuell() {
        LocalDate now = LocalDate.now();
        return !(now.isBefore(zeitraumStart) || now.isAfter(zeitraumEnde));
    }


    public boolean isPaymentRequierd() {
        return preis.isPaymentRequierd();
    }

    public BigInteger preisFor(PersonKategorie personKategorie) {
        return preis.preisFor(personKategorie);
    }

    public boolean hasEqualKursnummer(SportAngebot otherSportAngebot) {
        return this.getKursnummer().equals(otherSportAngebot.getKursnummer());
    }


    public Optional<SportTermin> findByDate(LocalDate terminDate) {
        return sportTermine.stream().filter(sportTermin -> sportTermin.getTerminDate().equals(terminDate)).findFirst();
    }


    @JsonIgnore
    public Stream<SportTermin> upcomingSportTermine() {
        return getSportTermine().stream()
                .filter(sportTermin -> sportTermin.getStartZeit().isAfter(LocalDateTime.now()))
                .sorted();
    }

    public void addSportTermin(SportTermin sportTermin) {
        sportTermin.setSportAngebot(this);
        this.sportTermine.add(sportTermin);
    }

    public void addAllSportTermine(Collection<SportTermin> sportTermine) {
        for (SportTermin sportTermin : sportTermine) {
            sportTermin.setSportAngebot(this);
            this.sportTermine.add(sportTermin);
        }
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

}
