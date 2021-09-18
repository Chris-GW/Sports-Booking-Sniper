package de.chrisgw.sportsbookingsniper.gui.teilnehmer;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerGender;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public class TeilnehmerFormPanel extends Panel {

    private final Map<Interactable, Label> fieldToLabelMap = new HashMap<>();

    private final TextBox vornameTextBox = new TextBox();
    private final TextBox nachnameTextBox = new TextBox();
    private final ComboBox<TeilnehmerGender> genderComboBox = createGenderComboBox();

    private final TextBox streetTextBox = new TextBox();
    private final TextBox ortTextBox = new TextBox();
    private final TextBox emailTextBox = new TextBox();
    private final TextBox telefonTextBox = new TextBox();

    private final ComboBox<TeilnehmerKategorie> kategorieComboBox = createTeilnehmerKategorieComboBox();
    private final EmptySpace kategoriePlaceHolder = new EmptySpace().setVisible(false);
    private final TextBox matrikelNummerTextBox = new TextBox();
    private final TextBox mitarbeiterNummerTextBox = new TextBox();

    private final TextBox ibanTextBox = new TextBox();
    private final TextBox kontoInhaberTextBox = new TextBox();


    public TeilnehmerFormPanel() {
        this(null);
    }

    public TeilnehmerFormPanel(Teilnehmer teilnehmer) {
        super();

        addFormularComponent(new Label("Bitte geben Sie die Angaben zum Teilnehmer ein:"));
        addFormularComponent(new EmptySpace());

        addFormularField("Vorname*", vornameTextBox);
        addFormularField("Nachname*", nachnameTextBox);
        addFormularField("Geschlecht*", genderComboBox);
        addFormularComponent(new EmptySpace());

        addFormularField("Straße*", streetTextBox);
        addFormularField("Ort*", ortTextBox);
        addFormularField("E-Mail*", emailTextBox);
        addFormularField("Telefon*", telefonTextBox);
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

        int maxLabelLength = fieldToLabelMap.values()
                .stream()
                .map(Label::getText)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        fieldToLabelMap.values().forEach(label -> label.setPreferredSize(new TerminalSize(maxLabelLength, 1)));
        setTeilnehmer(teilnehmer);
    }


    private <T extends Interactable> void addFormularField(String labelText, T interactable) {
        interactable.setLayoutData(createLayoutData(Fill, CanGrow));
        Label label = new Label(labelText);
        fieldToLabelMap.put(interactable, label);
        this.addComponent(Panels.horizontal(label, interactable), createLayoutData(Fill, CanGrow));
    }

    private <T extends Component> void addFormularComponent(T component) {
        this.addComponent(component, createLayoutData(Fill, CanGrow));
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


    public boolean validateTeilnehmerForm() {
        return List.of( //
                isEmptyTextBox(vornameTextBox), //
                isEmptyTextBox(nachnameTextBox), //
                isUnselectedComboBox(genderComboBox), //
                isEmptyTextBox(streetTextBox), //
                isEmptyTextBox(ortTextBox), //
                isEmptyTextBox(emailTextBox), //
                isEmptyTextBox(telefonTextBox), //
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
                isUnselectedComboBox(kategorieComboBox), //
                requiresMatrikelnummer && isEmptyTextBox(matrikelNummerTextBox), //
                requiresMitarbeiterNummer && isEmptyTextBox(mitarbeiterNummerTextBox) //
        ).contains(true);
    }

    private boolean isEmptyTextBox(TextBox textBox) {
        boolean hasError = StringUtils.isEmpty(trimToNull(textBox.getText()));
        setFieldFeedback(textBox, hasError);
        return hasError;
    }

    private <T> boolean isUnselectedComboBox(ComboBox<T> comboBox) {
        boolean hasError = comboBox.getSelectedItem() == null;
        setFieldFeedback(comboBox, hasError);
        return hasError;
    }

    private void setFieldFeedback(Interactable interactable, boolean hasError) {
        Label label = fieldToLabelMap.get(interactable);
        if (label != null && hasError) {
            label.setForegroundColor(ANSI.RED).setBackgroundColor(ANSI.WHITE);
        } else if (label != null) {
            label.setForegroundColor(null).setBackgroundColor(null);
        }
    }


    public Teilnehmer readTeilnehmer() {
        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVorname(trimToNull(vornameTextBox.getText()));
        teilnehmer.setNachname(trimToNull(nachnameTextBox.getText()));
        teilnehmer.setGender(genderComboBox.getSelectedItem());

        teilnehmer.setStreet(trimToNull(streetTextBox.getText()));
        teilnehmer.setOrt(trimToNull(ortTextBox.getText()));
        teilnehmer.setEmail(trimToNull(emailTextBox.getText()));
        teilnehmer.setTelefon(trimToNull(telefonTextBox.getText()));

        teilnehmer.setTeilnehmerKategorie(kategorieComboBox.getSelectedItem());
        teilnehmer.setMatrikelnummer(trimToNull(matrikelNummerTextBox.getText()));
        teilnehmer.setMitarbeiterNummer(trimToNull(mitarbeiterNummerTextBox.getText()));

        teilnehmer.setIban(trimToNull(ibanTextBox.getText()));
        teilnehmer.setKontoInhaber(trimToNull(kontoInhaberTextBox.getText()));
        return teilnehmer;
    }


    public void setTeilnehmer(Teilnehmer teilnehmer) {
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

        kategorieComboBox.setSelectedItem(teilnehmer.getTeilnehmerKategorie());
        matrikelNummerTextBox.setText(defaultString(teilnehmer.getMatrikelnummer()));
        mitarbeiterNummerTextBox.setText(defaultString(teilnehmer.getMitarbeiterNummer()));

        ibanTextBox.setText(defaultString(teilnehmer.getIban()));
        kontoInhaberTextBox.setText(defaultString(teilnehmer.getKontoInhaber()));
    }

    public void resetTeilnehmerForm() {
        setTeilnehmer(null);
    }

}
