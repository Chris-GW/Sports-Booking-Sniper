package de.chrisgw.sportsbookingsniper.buchung;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


@Data
public class Teilnehmer {

    private String vorname;
    private String nachname;
    private String street;
    private String ort;
    private String email;
    private String telefon;
    private TeilnehmerGender gender = TeilnehmerGender.KEINE_ANGABE;

    private TeilnehmerKategorie teilnehmerKategorie;
    private String matrikelnummer; // needed for PersonenKategorie Student
    private String mitarbeiterNummer; // needed for PersonenKategorie Mitarbeiter

    // needed for payment required SportAngebot
    private String iban;
    private String kontoInhaber;


    @JsonIgnore
    public String getName() {
        return getVorname() + " " + getNachname();
    }

    @Override
    public String toString() {
        return getName();
    }

}
