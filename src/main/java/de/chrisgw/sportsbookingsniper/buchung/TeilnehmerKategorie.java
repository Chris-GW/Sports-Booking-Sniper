package de.chrisgw.sportsbookingsniper.buchung;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum TeilnehmerKategorie {

    STUDENT_FH("StudentIn der FH", "S-FH"), //
    STUDENT_RWTH("StudentIn der RWTH", "S-RWTH"), //
    STUDENT_NRW("StudentIn in NRW", "S-NRW"), //
    STUDENT_ANDERE_HOCHSCHULE("StudentIn einer anderen Hochschule", "S-aH"), //
    RWTH_ALUMI("RWTH-Alumni", "Alumni"), //

    AZUBI_RWTH_UKA("Azubi RWTH/UKA", "Azubi"), //
    SCHUELER("Schüler", "Schüler"), //

    MITARBEITER_RWTH("Beschäftigte/r der RWTH", "B-RWTH"), //
    MITARBEITER_FH("Beschäftigte/r der FH", "B-FH"), //
    MITARBEITER_KLINIKUM("Beschäftigte/r Klinikum", "B-KLIN"), //
    EXTERN("Externe/r", "Extern"),
    ;


    private final String name;
    private final String value;

    TeilnehmerKategorie(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public static TeilnehmerKategorie fromKategorieName(String kategorieName) {
        for (TeilnehmerKategorie teilnehmerKategorie : TeilnehmerKategorie.values()) {
            if (teilnehmerKategorie.getName().equalsIgnoreCase(kategorieName)) {
                return teilnehmerKategorie;
            }
        }
        throw new IllegalArgumentException(kategorieName + " is not a valid kategorieName for a PersonKategorie");
    }

    @JsonCreator
    public static TeilnehmerKategorie fromKategorieValue(String kategorieValue) {
        for (TeilnehmerKategorie teilnehmerKategorie : TeilnehmerKategorie.values()) {
            if (teilnehmerKategorie.getValue().equals(kategorieValue)) {
                return teilnehmerKategorie;
            }
        }
        throw new IllegalArgumentException(kategorieValue + " is not a valid kategorieValue for a PersonKategorie");
    }


    public boolean requiresMatrikelnummer() {
        switch (this) {
        case STUDENT_FH:
        case STUDENT_RWTH:
        case STUDENT_NRW:
        case STUDENT_ANDERE_HOCHSCHULE:
            return true;
        default:
            return false;
        }
    }

    public boolean requiresMitarbeiterNummer() {
        switch (this) {
        case MITARBEITER_RWTH:
        case MITARBEITER_FH:
        case MITARBEITER_KLINIKUM:
            return true;
        default:
            return false;
        }
    }


    public String getName() {
        return name;
    }

    @JsonValue
    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        return this.name;
    }

}
