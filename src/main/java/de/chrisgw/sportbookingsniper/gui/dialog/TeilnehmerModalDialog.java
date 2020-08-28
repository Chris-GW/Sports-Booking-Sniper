package de.chrisgw.sportbookingsniper.gui.dialog;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer.Gender;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerValidator;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerKategorie;
import de.chrisgw.sportbookingsniper.gui.bind.ModalField;
import de.chrisgw.sportbookingsniper.gui.bind.ModalForm;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static de.chrisgw.sportbookingsniper.gui.bind.ModalFieldBuilder.newComboBoxField;
import static de.chrisgw.sportbookingsniper.gui.bind.ModalFieldBuilder.newTextBoxField;
import static java.util.Objects.requireNonNull;


@Slf4j
public class TeilnehmerModalDialog extends DialogWindow implements WindowListener {


    private final ApplicationStateDao applicationStateDao;
    private final boolean forceValidTeilnehmerForm;
    private Teilnehmer teilnehmer;
    private ModalForm modalForm = new ModalForm();


    public TeilnehmerModalDialog(ApplicationStateDao applicationStateDao) {
        this(applicationStateDao, false);
    }

    public TeilnehmerModalDialog(ApplicationStateDao applicationStateDao, boolean forceValidTeilnehmerForm) {
        super("Personenangaben");
        this.applicationStateDao = requireNonNull(applicationStateDao);
        this.forceValidTeilnehmerForm = forceValidTeilnehmerForm;
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setComponent(createContentPane());
        setTeilnehmer(applicationStateDao.getTeilnehmerListe().get(0));
        setCloseWindowWithEscape(!forceValidTeilnehmerForm);
    }


    private Panel createContentPane() {
        Panel contentPane = new Panel();
        contentPane.addComponent(createFormularPanel());
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel(), createLayoutData(End));
        addWindowListener(this);
        return contentPane;
    }


    private Panel createFormularPanel() {
        Panel formularGridPanel = new Panel(new GridLayout(2));
        formularGridPanel.addComponent(new Label("Bitte geben Sie die Daten des Teilnehmers ein:"),
                createHorizontallyFilledLayoutData(2));
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        newTextBoxField("vorname").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("nachname").addTo(modalForm).addToGrid(formularGridPanel);
        newComboBoxField("gender", Gender.values()).withLabel("Geschlecht:*")
                .addTo(modalForm)
                .addToGrid(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        newTextBoxField("street").withLabel("Straße:*").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("ort").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("email").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("telefon").withLabel("Telefon:").addTo(modalForm).addToGrid(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        createPersonenKategorieFields(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        formularGridPanel.addComponent(new Label("Konto, zum Bezahlen des Kursentgeltes per Lastschrift:"),
                createHorizontallyFilledLayoutData(2));
        newTextBoxField("iban").withLabel("IBAN:*").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("kontoInhaber").withLabel("KontoInhaber:").addTo(modalForm).addToGrid(formularGridPanel);
        formularGridPanel.addComponent(new Label("nur ändern, falls nicht mit Teilnehmer/in identisch"),
                createHorizontallyFilledLayoutData(2));
        return formularGridPanel;
    }

    private void createPersonenKategorieFields(Panel formularGridPanel) {
        ComboBox<TeilnehmerKategorie> kategorieComboBox = newComboBoxField("teilnehmerKategorie",
                TeilnehmerKategorie.values()) //
                .withLabel("Kategorie:*") //
                .addTo(modalForm) //
                .addToGrid(formularGridPanel) //
                .getInputField();

        ModalField<TextBox, String> matrikelnummerField = newTextBoxField("matrikelnummer") //
                .withLabel("Matrikel-Nr.:*") //
                .addTo(modalForm);
        ModalField<TextBox, String> mitarbeiterNummerField = newTextBoxField("mitarbeiterNummer") //
                .withLabel("Mitarbeiternr.:*") //
                .addTo(modalForm);

        kategorieComboBox.addListener((selectedIndex, previousSelection) -> {
            TeilnehmerKategorie selectedItem = kategorieComboBox.getItem(selectedIndex);
            matrikelnummerField.setVisible(selectedIndex >= 0 && selectedItem.requiresMatrikelnummer());
            mitarbeiterNummerField.setVisible(selectedIndex >= 0 && selectedItem.requiresMitarbeiterNummer());
        });
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        if (!forceValidTeilnehmerForm) {
            new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        }
        new Button("Reset", this::resetTeilnehmerForm).addTo(lowerButtonPanel);
        new Button("Save", this::saveTeilnehmer).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }


    private void resetTeilnehmerForm() {
        setTeilnehmer(new Teilnehmer());
    }


    private void saveTeilnehmer() {
        BindingResult bindingResult = bindTeilnehmerModalData();

        if (!bindingResult.hasErrors()) {
            this.teilnehmer = (Teilnehmer) bindingResult.getTarget();
            this.close();
        }
    }


    private BindingResult bindTeilnehmerModalData() {
        DataBinder dataBinder = new DataBinder(new Teilnehmer());
        dataBinder.setAllowedFields("vorname", "nachname", "email", "telefon", "gender", "street", "ort",
                "teilnehmerKategorie", "matrikelnummer", "mitarbeiterNummer", "iban", "kontoInhaber");
        dataBinder.addValidators(new TeilnehmerValidator());
        return modalForm.bindData(dataBinder);
    }


    public void setTeilnehmer(Teilnehmer teilnehmer) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(teilnehmer);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            propertyValues.addPropertyValue(propertyName, propertyValue);
        }
        modalForm.writePropertyValues(propertyValues);
    }


    public Optional<Teilnehmer> getTeilnehmer() {
        return Optional.ofNullable(teilnehmer);
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
            saveTeilnehmer();
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
        return getTeilnehmer();
    }

}
