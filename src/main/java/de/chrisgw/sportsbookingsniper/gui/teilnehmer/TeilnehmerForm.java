package de.chrisgw.sportsbookingsniper.gui.teilnehmer;

import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.TextBox;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerGender;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import de.chrisgw.sportsbookingsniper.gui.component.FormPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.DATE_FORMATTER;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public class TeilnehmerForm extends FormPanel<Teilnehmer> {

    private final TextBox vornameTextBox = new TextBox();
    private final TextBox nachnameTextBox = new TextBox();
    private final ComboBox<TeilnehmerGender> genderComboBox = createGenderComboBox();

    private final TextBox streetTextBox = new TextBox();
    private final TextBox ortTextBox = new TextBox();
    private final TextBox emailTextBox = new TextBox();
    private final TextBox geburtsdatumTextBox = new TextBox();
    private final TextBox telefonTextBox = new TextBox();

    private final ComboBox<TeilnehmerKategorie> kategorieComboBox = createTeilnehmerKategorieComboBox();
    private final EmptySpace kategoriePlaceHolder = new EmptySpace().setVisible(false);
    private final TextBox matrikelNummerTextBox = new TextBox();
    private final TextBox mitarbeiterNummerTextBox = new TextBox();

    private final TextBox ibanTextBox = new TextBox();
    private final TextBox kontoInhaberTextBox = new TextBox();


    public TeilnehmerForm() {
        this(null);
    }

    public TeilnehmerForm(Teilnehmer teilnehmer) {
        super();
        geburtsdatumTextBox.setValidationPattern(Pattern.compile("[0-9.]{0,10}"));

        addFormularField("Vorname*", vornameTextBox);
        addFormularField("Nachname*", nachnameTextBox);
        addFormularField("Geschlecht*", genderComboBox);
        addFormularComponent(new EmptySpace());

        addFormularField("Straße*", streetTextBox);
        addFormularField("Ort*", ortTextBox);
        addFormularField("E-Mail*", emailTextBox);
        addFormularField("Telefon*", telefonTextBox);
        addFormularField("Geburtsdatum", geburtsdatumTextBox);
        addFormularComponent(new EmptySpace());

        addFormularField("Kategorie*", kategorieComboBox);
        addFormularComponent(kategoriePlaceHolder);
        addFormularField("Matrikelnr.*", matrikelNummerTextBox);
        addFormularField("dienst. Tel.*", mitarbeiterNummerTextBox);
        addFormularComponent(new EmptySpace());

        addFormularComponent(new Label("Konto, zum Bezahlen des Kursentgeltes per Lastschrift:"));
        addFormularField("IBAN*", ibanTextBox);
        addFormularField("Kontoinhaber", kontoInhaberTextBox);
        addFormularComponent(new Label("nur ändern, falls nicht mit Teilnehmer/in identisch"));
        setFormValue(teilnehmer);
    }


    private ComboBox<TeilnehmerGender> createGenderComboBox() {
        ComboBox<TeilnehmerGender> genderComboBox = new ComboBox<>();
        Arrays.stream(TeilnehmerGender.values()).forEachOrdered(genderComboBox::addItem);
        return genderComboBox;
    }

    private ComboBox<TeilnehmerKategorie> createTeilnehmerKategorieComboBox() {
        ComboBox<TeilnehmerKategorie> kategorieComboBox = new ComboBox<>();
        Arrays.stream(TeilnehmerKategorie.values()).forEachOrdered(kategorieComboBox::addItem);

        kategorieComboBox.addListener((selectedIndex, previousSelection, changedByUserInteraction) -> {
            TeilnehmerKategorie selectedItem = kategorieComboBox.getSelectedItem();
            boolean requiresMatrikelnummer = selectedItem != null && selectedItem.requiresMatrikelnummer();
            boolean requiresMitarbeiterNummer = selectedItem != null && selectedItem.requiresMitarbeiterNummer();
            matrikelNummerTextBox.getParent().setVisible(requiresMatrikelnummer);
            mitarbeiterNummerTextBox.getParent().setVisible(requiresMitarbeiterNummer);
            kategoriePlaceHolder.setVisible(!requiresMatrikelnummer && !requiresMitarbeiterNummer);
        });
        return kategorieComboBox;
    }


    @Override
    public boolean validateForm() {
        return List.of( //
                isEmpty(vornameTextBox), //
                isEmpty(nachnameTextBox), //
                isUnselected(genderComboBox), //
                isEmpty(streetTextBox), //
                isEmpty(ortTextBox), //
                isEmpty(emailTextBox), //
                isEmpty(telefonTextBox), //
                isInvalidDateTimeFormat(geburtsdatumTextBox, DATE_FORMATTER), //
                validateTeilnehmerKategorieForm() //
        ).contains(true);
    }


    private boolean validateTeilnehmerKategorieForm() {
        setFieldFeedback(matrikelNummerTextBox, false);
        setFieldFeedback(mitarbeiterNummerTextBox, false);

        TeilnehmerKategorie selectedKategorie = kategorieComboBox.getSelectedItem();
        boolean requiresMatrikelnummer = selectedKategorie != null && selectedKategorie.requiresMatrikelnummer();
        boolean requiresMitarbeiterNummer = selectedKategorie != null && selectedKategorie.requiresMitarbeiterNummer();
        return List.of( //
                isUnselected(kategorieComboBox), //
                requiresMatrikelnummer && isEmpty(matrikelNummerTextBox), //
                requiresMitarbeiterNummer && isEmpty(mitarbeiterNummerTextBox) //
        ).contains(true);
    }


    @Override
    public Teilnehmer readFormValue() {
        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVorname(trimToNull(vornameTextBox.getText()));
        teilnehmer.setNachname(trimToNull(nachnameTextBox.getText()));
        teilnehmer.setGender(genderComboBox.getSelectedItem());

        teilnehmer.setStreet(trimToNull(streetTextBox.getText()));
        teilnehmer.setOrt(trimToNull(ortTextBox.getText()));
        teilnehmer.setEmail(trimToNull(emailTextBox.getText()));
        teilnehmer.setTelefon(trimToNull(telefonTextBox.getText()));
        setTeilnehmerGeburtsDatum(teilnehmer);

        teilnehmer.setTeilnehmerKategorie(kategorieComboBox.getSelectedItem());
        teilnehmer.setMatrikelnummer(trimToNull(matrikelNummerTextBox.getText()));
        teilnehmer.setMitarbeiterNummer(trimToNull(mitarbeiterNummerTextBox.getText()));

        teilnehmer.setIban(trimToNull(ibanTextBox.getText()));
        teilnehmer.setKontoInhaber(trimToNull(kontoInhaberTextBox.getText()));
        return teilnehmer;
    }

    private void setTeilnehmerGeburtsDatum(Teilnehmer teilnehmer) {
        String geburtsdatumStr = trimToNull(geburtsdatumTextBox.getText());
        if (geburtsdatumStr != null) {
            teilnehmer.setGeburtsDatum(LocalDate.parse(geburtsdatumStr, DATE_FORMATTER));
        }
    }


    @Override
    public void setFormValue(Teilnehmer teilnehmer) {
        if (teilnehmer == null) {
            teilnehmer = new Teilnehmer();
        }
        vornameTextBox.setText(defaultString(teilnehmer.getVorname()));
        nachnameTextBox.setText(defaultString(teilnehmer.getNachname()));
        genderComboBox.setSelectedItem(teilnehmer.getGender());

        streetTextBox.setText(defaultString(teilnehmer.getStreet()));
        ortTextBox.setText(defaultString(teilnehmer.getOrt()));
        emailTextBox.setText(defaultString(teilnehmer.getEmail()));
        telefonTextBox.setText(defaultString(teilnehmer.getTelefon()));
        setGeburtsDatum(teilnehmer.getGeburtsDatum());

        kategorieComboBox.setSelectedItem(teilnehmer.getTeilnehmerKategorie());
        matrikelNummerTextBox.setText(defaultString(teilnehmer.getMatrikelnummer()));
        mitarbeiterNummerTextBox.setText(defaultString(teilnehmer.getMitarbeiterNummer()));

        ibanTextBox.setText(defaultString(teilnehmer.getIban()));
        kontoInhaberTextBox.setText(defaultString(teilnehmer.getKontoInhaber()));
    }

    private void setGeburtsDatum(LocalDate geburtsDatum) {
        if (geburtsDatum != null) {
            geburtsdatumTextBox.setText(DATE_FORMATTER.format(geburtsDatum));
        } else {
            geburtsdatumTextBox.setText("");
        }
    }

}
