package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer.Gender;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import de.chrisgw.sportsbookingsniper.gui.component.ShortKeyRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public class TeilnehmerFormDialog extends DialogWindow {

    private boolean forceValidTeilnehmerForm;
    private final AtomicBoolean saved = new AtomicBoolean(false);
    private final ShortKeyRegistry shortKeyRegistry = new ShortKeyRegistry();
    private final Map<Interactable, Label> fieldToLabelMap = new HashMap<>();

    private final Panel formularPanel = new Panel();
    private final TextBox vornameTextBox = new TextBox();
    private final TextBox nachnameTextBox = new TextBox();
    private final ComboBox<Gender> genderComboBox = createGenderComboBox();

    private final TextBox streetTextBox = new TextBox();
    private final TextBox ortTextBox = new TextBox();
    private final TextBox emailTextBox = new TextBox();
    private final TextBox telefonTextBox = new TextBox();

    private final ComboBox<TeilnehmerKategorie> kategorieComboBox = createTeilnehmerKategorieComboBox();
    private final TextBox matrikelNummerTextBox = new TextBox();
    private final TextBox mitarbeiterNummerTextBox = new TextBox();

    private final TextBox ibanTextBox = new TextBox();
    private final TextBox kontoInhaberTextBox = new TextBox();

    private final Button cancelBtn = new Button(LocalizedString.Cancel.toString(), this::close);
    private final Button resetBtn = new Button("zurücksetzen", this::resetTeilnehmerForm);
    private final Button saveBtn = new Button(LocalizedString.Save.toString(), this::saveTeilnehmerForm);


    public TeilnehmerFormDialog() {
        this(null);
    }

    public TeilnehmerFormDialog(Teilnehmer teilnehmer) {
        super("Personenangaben");
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setForceValidTeilnehmerForm(false);
        addBasePaneListener(shortKeyRegistry);

        Panel contentPane = new Panel();
        contentPane.addComponent(initalizeFormularGridPanel());
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel());
        setComponent(contentPane);
        setTeilnehmer(teilnehmer);
    }


    private Panel initalizeFormularGridPanel() {
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
        addFormularField("Matrikelnr.*", matrikelNummerTextBox);
        addFormularField("dienst. Tel.*", mitarbeiterNummerTextBox);
        addFormularComponent(new EmptySpace());

        addFormularComponent(new Label("Konto, zum Bezahlen des Kursentgeltes per Lastschrift:"));
        addFormularField("IBAN*", ibanTextBox);
        addFormularField("Kontoinhaber*", kontoInhaberTextBox);
        addFormularComponent(new Label("nur ändern, falls nicht mit Teilnehmer/in identisch"));

        int maxLabelLength = fieldToLabelMap.values()
                .stream()
                .map(Label::getText)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        fieldToLabelMap.values().forEach(label -> label.setPreferredSize(new TerminalSize(maxLabelLength, 1)));
        return formularPanel;
    }

    private <T extends Interactable> void addFormularField(String labelText, T interactable) {
        interactable.setLayoutData(createLayoutData(Fill, CanGrow));
        Label label = new Label(labelText);
        fieldToLabelMap.put(interactable, label);
        formularPanel.addComponent(Panels.horizontal(label, interactable), createLayoutData(Fill, CanGrow));
    }

    private <T extends Component> void addFormularComponent(T component) {
        formularPanel.addComponent(component, createLayoutData(Fill, CanGrow));
    }


    private ComboBox<Gender> createGenderComboBox() {
        ComboBox<Gender> genderComboBox = new ComboBox<>();
        for (Gender gender : Gender.values()) {
            genderComboBox.addItem(gender);
        }
        return genderComboBox;
    }

    private ComboBox<TeilnehmerKategorie> createTeilnehmerKategorieComboBox() {
        ComboBox<TeilnehmerKategorie> kategorieComboBox = new ComboBox<>();
        for (TeilnehmerKategorie kategorie : TeilnehmerKategorie.values()) {
            kategorieComboBox.addItem(kategorie);
        }

        kategorieComboBox.addListener((selectedIndex, previousSelection) -> {
            TeilnehmerKategorie selectedItem = kategorieComboBox.getSelectedItem();
            boolean requiresMatrikelnummer = selectedItem != null && selectedItem.requiresMatrikelnummer();
            boolean requiresMitarbeiterNummer = selectedItem != null && selectedItem.requiresMitarbeiterNummer();
            matrikelNummerTextBox.getParent().setVisible(requiresMatrikelnummer);
            mitarbeiterNummerTextBox.getParent().setVisible(requiresMitarbeiterNummer);
        });
        return kategorieComboBox;
    }


    private Panel createLowerButtonPanel() {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(HORIZONTAL).setSpacing(1));
        buttonPanel.addComponent(cancelBtn);
        buttonPanel.addComponent(new EmptySpace(), createLayoutData(Fill, CanGrow));
        buttonPanel.addComponent(resetBtn);
        buttonPanel.addComponent(saveBtn);
        shortKeyRegistry.registerButtonAction(new KeyStroke('z', true, false), resetBtn);
        shortKeyRegistry.registerButtonAction(new KeyStroke('s', true, false), saveBtn);
        return buttonPanel.setLayoutData(createLayoutData(Fill, CanGrow));
    }

    private void saveTeilnehmerForm() {
        if (validateTeilnehmerForm()) {
            saved.set(true);
            this.close();
        }
    }

    protected boolean validateTeilnehmerForm() {
        boolean hasError;
        hasError = isEmptyTextBox(vornameTextBox);
        hasError = isEmptyTextBox(nachnameTextBox) || hasError;
        hasError = isUnselectedComboBox(genderComboBox) || hasError;

        hasError = isEmptyTextBox(streetTextBox) || hasError;
        hasError = isEmptyTextBox(ortTextBox) || hasError;
        hasError = isEmptyTextBox(emailTextBox) || hasError;
        hasError = isEmptyTextBox(telefonTextBox) || hasError;
        hasError = validateTeilnehmerKategorieForm() || hasError;
        return !hasError;
    }

    private boolean validateTeilnehmerKategorieForm() {
        setFieldFeedback(matrikelNummerTextBox, false);
        setFieldFeedback(mitarbeiterNummerTextBox, false);

        boolean hasError = isUnselectedComboBox(kategorieComboBox);
        TeilnehmerKategorie selectedKategorie = kategorieComboBox.getSelectedItem();
        if (selectedKategorie != null && selectedKategorie.requiresMatrikelnummer()) {
            hasError = isEmptyTextBox(matrikelNummerTextBox) || hasError;
        } else if (selectedKategorie != null && selectedKategorie.requiresMitarbeiterNummer()) {
            hasError = isEmptyTextBox(mitarbeiterNummerTextBox) || hasError;
        }
        return hasError;
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


    public boolean isForceValidTeilnehmerForm() {
        return forceValidTeilnehmerForm;
    }

    public void setForceValidTeilnehmerForm(boolean forceValidTeilnehmerForm) {
        this.forceValidTeilnehmerForm = forceValidTeilnehmerForm;
        setCloseWindowWithEscape(!forceValidTeilnehmerForm);
        cancelBtn.setEnabled(!forceValidTeilnehmerForm);
    }


    @Override
    public Optional<Teilnehmer> showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        if (saved.get()) {
            return Optional.of(readTeilnehmer());
        } else {
            return Optional.empty();
        }
    }

}
