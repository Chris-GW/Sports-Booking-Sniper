package de.chrisgw.sportbooking.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum PersonKategorie {

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


    private String name;
    private String value;

    PersonKategorie(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public static PersonKategorie fromKategorieName(String kategorieName) {
        for (PersonKategorie personKategorie : PersonKategorie.values()) {
            if (personKategorie.getName().equalsIgnoreCase(kategorieName)) {
                return personKategorie;
            }
        }
        throw new IllegalArgumentException(kategorieName + " is not a valid kategorieName for a PersonKategorie");
    }

    @JsonCreator
    public static PersonKategorie fromKategorieValue(String kategorieValue) {
        for (PersonKategorie personKategorie : PersonKategorie.values()) {
            if (personKategorie.getValue().equals(kategorieValue)) {
                return personKategorie;
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
