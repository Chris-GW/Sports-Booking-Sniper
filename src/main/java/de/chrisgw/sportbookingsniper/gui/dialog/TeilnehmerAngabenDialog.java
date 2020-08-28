package de.chrisgw.sportbookingsniper.gui.dialog;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportbookingsniper.gui.bind.ConcealableComponent;
import de.chrisgw.sportbookingsniper.gui.bind.ModalField;
import de.chrisgw.sportbookingsniper.gui.bind.ModalForm;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngabenValidator;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerKategorie;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben.Gender;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportbookingsniper.gui.state.TeilnehmerAngabenListener;
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
public class TeilnehmerAngabenDialog extends DialogWindow implements WindowListener, TeilnehmerAngabenListener {


    private final ApplicationStateDao applicationDataService;
    private final boolean forceValidPersonenAngaben;
    private TeilnehmerAngaben teilnehmerAngaben;
    private ModalForm modalForm = new ModalForm();


    public TeilnehmerAngabenDialog(ApplicationStateDao applicationDataService) {
        this(applicationDataService, false);
    }

    public TeilnehmerAngabenDialog(ApplicationStateDao applicationDataService, boolean forceValidPersonenAngaben) {
        super("Personenangaben");
        this.applicationDataService = requireNonNull(applicationDataService);
        this.applicationDataService.addTeilnehmerAngabenListener(this);
        this.forceValidPersonenAngaben = forceValidPersonenAngaben;
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setComponent(createContentPane());
        setPersonenAngaben(applicationDataService.getTeilnehmerAngaben());
        setCloseWindowWithEscape(!forceValidPersonenAngaben);
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
        ComboBox<TeilnehmerKategorie> kategorieComboBox = newComboBoxField("teilnehmerKategorie", TeilnehmerKategorie.values()) //
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

        ConcealableComponent label = new ConcealableComponent().addTo(formularGridPanel);
        ConcealableComponent input = new ConcealableComponent() //
                .setLayoutData(createHorizontallyFilledLayoutData(1)) //
                .addTo(formularGridPanel);
        ConcealableComponent errorLabel = new ConcealableComponent() //
                .setLayoutData(createHorizontallyFilledLayoutData(2)) //
                .addTo(formularGridPanel);

        kategorieComboBox.addListener((selectedIndex, previousSelection) -> {
            if (selectedIndex >= 0 && kategorieComboBox.getItem(selectedIndex).requiresMatrikelnummer()) {
                label.setComponent(matrikelnummerField.getLabel());
                input.setComponent(matrikelnummerField.getInputField());
                errorLabel.setComponent(matrikelnummerField.getConcealableErrorLabel());
            } else if (selectedIndex >= 0 && kategorieComboBox.getItem(selectedIndex).requiresMitarbeiterNummer()) {
                label.setComponent(mitarbeiterNummerField.getLabel());
                input.setComponent(mitarbeiterNummerField.getInputField());
                errorLabel.setComponent(mitarbeiterNummerField.getConcealableErrorLabel());
            } else {
                label.setComponent(null);
                input.setComponent(null);
                errorLabel.setComponent(null);
            }
        });
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        if (!forceValidPersonenAngaben) {
            new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        }
        new Button("Reset", this::resetPersonenAngaben).addTo(lowerButtonPanel);
        new Button("Save", this::savePersonenAngaben).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }


    private void resetPersonenAngaben() {
        setPersonenAngaben(new TeilnehmerAngaben());
    }


    private void savePersonenAngaben() {
        BindingResult bindingResult = bindPersonenAngabenModalData();

        if (!bindingResult.hasErrors()) {
            this.teilnehmerAngaben = (TeilnehmerAngaben) bindingResult.getTarget();
            this.close();
        }
    }


    private BindingResult bindPersonenAngabenModalData() {
        DataBinder dataBinder = new DataBinder(new TeilnehmerAngaben());
        dataBinder.setAllowedFields("vorname", "nachname", "email", "telefon", "gender", "street", "ort",
                "teilnehmerKategorie", "matrikelnummer", "mitarbeiterNummer", "iban", "kontoInhaber");
        dataBinder.addValidators(new TeilnehmerAngabenValidator());
        return modalForm.bindData(dataBinder);
    }


    public void setPersonenAngaben(TeilnehmerAngaben teilnehmerAngaben) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(teilnehmerAngaben);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            propertyValues.addPropertyValue(propertyName, propertyValue);
        }
        modalForm.writePropertyValues(propertyValues);
    }


    public Optional<TeilnehmerAngaben> getPersonenAngaben() {
        return Optional.ofNullable(teilnehmerAngaben);
    }


    @Override
    public void close() {
        applicationDataService.removeTeilnehmerAngabenListener(this);
        super.close();
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
            savePersonenAngaben();
            deliverEvent.set(false);
        } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'r') {
            resetPersonenAngaben();
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
    public void onChangedTeilnehmerAngaben(TeilnehmerAngaben changedTeilnehmerAngaben) {
        setPersonenAngaben(changedTeilnehmerAngaben);
    }


    @Override
    public Optional<TeilnehmerAngaben> showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        return getPersonenAngaben();
    }

}
