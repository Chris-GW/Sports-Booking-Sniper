package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer.Gender;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public class TeilnehmerFormDialog extends DialogWindow implements WindowListener {

    @Getter
    private boolean forceValidTeilnehmerForm;
    private AtomicBoolean saved = new AtomicBoolean(false);

    private Panel formularGridPanel = createFormularGridPanel();
    private TextBox vornameTextBox = new TextBox();
    private TextBox nachnameTextBox = new TextBox();
    private ComboBox<Gender> genderComboBox = createGenderComboBox();

    private TextBox streetTextBox = new TextBox();
    private TextBox ortTextBox = new TextBox();
    private TextBox emailTextBox = new TextBox();
    private TextBox telefonTextBox = new TextBox();

    private ComboBox<TeilnehmerKategorie> kategorieComboBox = createTeilnehmerKategorieComboBox();
    private TextBox matrikelNummerTextBox = new TextBox();
    private TextBox mitarbeiterNummerTextBox = new TextBox();

    private TextBox ibanTextBox = new TextBox();
    private TextBox kontoInhaberTextBox = new TextBox();


    public TeilnehmerFormDialog() {
        super("Personenangaben");
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setForceValidTeilnehmerForm(false);
        addWindowListener(this);

        Panel contentPane = new Panel();
        contentPane.addComponent(formularGridPanel);
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel(), createLayoutData(End));
        setComponent(contentPane);
    }


    private Panel createFormularGridPanel() {
        Panel formularGridPanel = new Panel(new GridLayout(2));
        formularGridPanel.addComponent(new Label("Bitte geben Sie die Daten des Teilnehmers ein:"),
                createHorizontallyFilledLayoutData(2));
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        formularGridPanel.addComponent(new Label("Vorname*")).addComponent(vornameTextBox);
        formularGridPanel.addComponent(new Label("Nachname*")).addComponent(nachnameTextBox);
        formularGridPanel.addComponent(new Label("Geschlecht*")).addComponent(genderComboBox);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        formularGridPanel.addComponent(new Label("Straße*")).addComponent(streetTextBox);
        formularGridPanel.addComponent(new Label("Ort*")).addComponent(ortTextBox);
        formularGridPanel.addComponent(new Label("E-Mail*")).addComponent(emailTextBox);
        formularGridPanel.addComponent(new Label("Telefon*")).addComponent(telefonTextBox);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        formularGridPanel.addComponent(new Label("Kategorie*")).addComponent(kategorieComboBox);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        formularGridPanel.addComponent(new Label("Konto, zum Bezahlen des Kursentgeltes per Lastschrift:"),
                createHorizontallyFilledLayoutData(2));
        formularGridPanel.addComponent(new Label("IBAN*")).addComponent(ibanTextBox);
        formularGridPanel.addComponent(new Label("Kontoinhaber*")).addComponent(kontoInhaberTextBox);
        formularGridPanel.addComponent(new Label("nur ändern, falls nicht mit Teilnehmer/in identisch"),
                createHorizontallyFilledLayoutData(2));
        return formularGridPanel;
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
            matrikelNummerTextBox.setVisible(requiresMatrikelnummer);
            findLabelFor(matrikelNummerTextBox).ifPresent(label -> label.setVisible(requiresMatrikelnummer));

            boolean requiresMitarbeiterNummer = selectedItem != null && selectedItem.requiresMitarbeiterNummer();
            mitarbeiterNummerTextBox.setVisible(requiresMitarbeiterNummer);
            findLabelFor(mitarbeiterNummerTextBox).ifPresent(label -> label.setVisible(requiresMitarbeiterNummer));
        });
        return kategorieComboBox;
    }


    private Optional<Label> findLabelFor(Interactable interactable) {
        int childIndex = formularGridPanel.getChildrenList().indexOf(interactable);
        if (childIndex > 0) {
            Component labelComponent = formularGridPanel.getChildrenList().get(childIndex - 1);
            if (labelComponent instanceof Label) {
                return Optional.of((Label) labelComponent);
            }
        }
        return Optional.empty();
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        if (!forceValidTeilnehmerForm) {
            new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        }
        new Button("Reset", this::resetTeilnehmerForm).addTo(lowerButtonPanel);
        new Button("Save", this::saveTeilnehmerForm).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }

    private void saveTeilnehmerForm() {
        if (isValidTeilnehmerForm()) {
            saved.set(true);
            this.close();
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

    public boolean isValidTeilnehmerForm() {
        // TODO validateTeilnehmer
        return true;
    }


    public void setTeilnehmer(Teilnehmer teilnehmer) {
        if (teilnehmer == null) {
            teilnehmer = new Teilnehmer();
        }
        vornameTextBox.setText(teilnehmer.getVorname());
        nachnameTextBox.setText(teilnehmer.getNachname());
        genderComboBox.setSelectedItem(teilnehmer.getGender());

        streetTextBox.setText(teilnehmer.getStreet());
        ortTextBox.setText(teilnehmer.getOrt());
        emailTextBox.setText(teilnehmer.getEmail());
        telefonTextBox.setText(teilnehmer.getTelefon());

        kategorieComboBox.setSelectedItem(teilnehmer.getTeilnehmerKategorie());
        matrikelNummerTextBox.setText(teilnehmer.getMatrikelnummer());
        mitarbeiterNummerTextBox.setText(teilnehmer.getMitarbeiterNummer());

        ibanTextBox.setText(teilnehmer.getIban());
        kontoInhaberTextBox.setText(teilnehmer.getKontoInhaber());
    }


    public void resetTeilnehmerForm() {
        setTeilnehmer(null);
    }


    public void setForceValidTeilnehmerForm(boolean forceValidTeilnehmerForm) {
        this.forceValidTeilnehmerForm = forceValidTeilnehmerForm;
        setCloseWindowWithEscape(!forceValidTeilnehmerForm);
    }


    @Override
    public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
        // noop
    }

    @Override
    public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
        // noop
    }

    @Override
    public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 's') {
            saveTeilnehmerForm();
            deliverEvent.set(false);
        } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'r') {
            resetTeilnehmerForm();
            deliverEvent.set(false);
        } else {
            deliverEvent.set(true);
        }
    }

    @Override
    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        // noop
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
