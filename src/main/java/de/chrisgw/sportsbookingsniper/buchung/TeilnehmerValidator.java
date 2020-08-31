package de.chrisgw.sportsbookingsniper.buchung;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.validation.ValidationUtils.rejectIfEmpty;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;


public class TeilnehmerValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Teilnehmer.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Teilnehmer teilnehmer = (Teilnehmer) target;
        rejectIfEmptyOrWhitespace(errors, "vorname", "vorname.empty");
        rejectIfEmptyOrWhitespace(errors, "nachname", "nachname.empty");
        rejectIfEmptyOrWhitespace(errors, "email", "email.empty");
        rejectIfEmpty(errors, "gender", "gender.empty");
        rejectIfEmptyOrWhitespace(errors, "street", "street.empty");
        rejectIfEmptyOrWhitespace(errors, "ort", "ort.empty");
        rejectIfEmptyOrWhitespace(errors, "telefon", "telefon.empty");

        rejectIfEmpty(errors, "teilnehmerKategorie", "teilnehmerKategorie.empty");
        TeilnehmerKategorie teilnehmerKategorie = teilnehmer.getTeilnehmerKategorie();
        if (teilnehmerKategorie != null && teilnehmerKategorie.requiresMatrikelnummer()) {
            rejectIfEmptyOrWhitespace(errors, "matrikelnummer", "matrikelnummer.empty");
        }
        if (teilnehmerKategorie != null && teilnehmerKategorie.requiresMitarbeiterNummer()) {
            rejectIfEmptyOrWhitespace(errors, "mitarbeiterNummer", "mitarbeiterNummer.empty");
        }

        rejectIfEmptyOrWhitespace(errors, "iban", "iban.empty");
    }

}
