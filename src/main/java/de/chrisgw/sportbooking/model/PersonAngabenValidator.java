package de.chrisgw.sportbooking.model;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.validation.ValidationUtils.rejectIfEmpty;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;


public class PersonAngabenValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonenAngaben.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonenAngaben personenAngaben = (PersonenAngaben) target;
        rejectIfEmptyOrWhitespace(errors, "vorname", "vorname.empty");
        rejectIfEmptyOrWhitespace(errors, "nachname", "nachname.empty");
        rejectIfEmptyOrWhitespace(errors, "email", "email.empty");
        rejectIfEmpty(errors, "gender", "gender.empty");
        rejectIfEmptyOrWhitespace(errors, "street", "street.empty");
        rejectIfEmptyOrWhitespace(errors, "ort", "ort.empty");

        rejectIfEmpty(errors, "personKategorie", "personKategorie.empty");
        PersonKategorie personKategorie = personenAngaben.getPersonKategorie();
        if (personKategorie != null && personKategorie.requiresMatrikelnummer()) {
            rejectIfEmptyOrWhitespace(errors, "matrikelnummer", "matrikelnummer.empty");
        }
        if (personKategorie != null && personKategorie.requiresMitarbeiterNummer()) {
            rejectIfEmptyOrWhitespace(errors, "mitarbeiterNummer", "mitarbeiterNummer.empty");
        }

        rejectIfEmptyOrWhitespace(errors, "iban", "iban.empty");
    }

}
