package de.chrisgw.sportbooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


@Data
public class PersonenAngaben {

    private String vorname;
    private String nachname;
    private String email;
    private String telefon;
    private Gender gender = Gender.MALE;

    private String street;
    private String ort;

    private PersonKategorie personKategorie;
    private String matrikelnummer; // needed for PersonenKategorie Student
    private String mitarbeiterNummer; // needed for PersonenKategorie Mitarbeiter

    // needed for payment required SportAngebot
    private String iban;
    private String kontoInhaber;


    @JsonIgnore
    public String getName() {
        return getVorname() + " " + getNachname();
    }


    public enum Gender {

        MALE("M", "männlich"), FEMALE("W", "weiblich");

        private String shortName;
        private String name;


        Gender(String shortName, String name) {
            this.shortName = shortName;
            this.name = name;
        }


        public static Gender fromShortname(String shortName) {
            for (Gender gender : Gender.values()) {
                if (gender.getShortName().equalsIgnoreCase(shortName)) {
                    return gender;
                }
            }
            throw new IllegalArgumentException(shortName + " is not a valid shortname for a Gender");
        }

        public static Gender fromName(String shortName) {
            for (Gender gender : Gender.values()) {
                if (gender.getName().equalsIgnoreCase(shortName)) {
                    return gender;
                }
            }
            throw new IllegalArgumentException(shortName + " is not a valid shortname for a Gender");
        }


        public String getShortName() {
            return shortName;
        }

        public String getName() {
            return name;
        }


        @Override
        public String toString() {
            return this.name;
        }

    }

}
