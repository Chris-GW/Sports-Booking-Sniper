package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.gui.bind.ConcealableComponent;
import de.chrisgw.sportbooking.gui.bind.ModalField;
import de.chrisgw.sportbooking.gui.bind.ModalForm;
import de.chrisgw.sportbooking.model.PersonAngabenValidator;
import de.chrisgw.sportbooking.model.PersonKategorie;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.model.PersonenAngaben.Gender;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.ApplicationStateDao.PersonenAngabenListener;
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

import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.END;
import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.GridLayout.createLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static de.chrisgw.sportbooking.gui.bind.ModalFieldBuilder.newComboBoxField;
import static de.chrisgw.sportbooking.gui.bind.ModalFieldBuilder.newTextBoxField;
import static java.util.Objects.requireNonNull;


@Slf4j
public class PersonenAngabenDialog extends DialogWindow implements WindowListener, PersonenAngabenListener {


    private final ApplicationStateDao applicationDataService;
    private final boolean forceValidPersonenAngaben;
    private PersonenAngaben personenAngaben;
    private ModalForm modalForm = new ModalForm();


    public PersonenAngabenDialog(ApplicationStateDao applicationDataService) {
        this(applicationDataService, false);
    }

    public PersonenAngabenDialog(ApplicationStateDao applicationDataService, boolean forceValidPersonenAngaben) {
        super("Personenangaben");
        this.applicationDataService = requireNonNull(applicationDataService);
        this.applicationDataService.addPersonenAngabenListener(this);
        this.forceValidPersonenAngaben = forceValidPersonenAngaben;
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        createContentPane();
        setPersonenAngaben(applicationDataService.getPersonenAngaben());
        setCloseWindowWithEscape(true);
    }


    private void createContentPane() {
        Panel contentPane = new Panel();
        contentPane.addComponent(createFormularPanel());
        contentPane.addComponent(createLowerButtonPanel(), createLayoutData(Fill));
        addWindowListener(this);
        setComponent(contentPane);
    }


    private Panel createFormularPanel() {
        Panel formularGridPanel = new Panel(new GridLayout(2));

        newTextBoxField("vorname").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("nachname").addTo(modalForm).addToGrid(formularGridPanel);
        newComboBoxField("gender", Gender.values()).withLabel("Geschlecht")
                .addTo(modalForm)
                .addToGrid(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        newTextBoxField("street").withLabel("Straße").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("ort").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("email").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("telefon").addTo(modalForm).addToGrid(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        createPersonenKategorieFields(formularGridPanel);
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        newTextBoxField("iban").withLabel("IBAN").addTo(modalForm).addToGrid(formularGridPanel);
        newTextBoxField("kontoInhaber").addTo(modalForm).addToGrid(formularGridPanel);
        return formularGridPanel;
    }

    private void createPersonenKategorieFields(Panel formularGridPanel) {
        ComboBox<PersonKategorie> kategorieComboBox = newComboBoxField("personKategorie", PersonKategorie.values()) //
                .withLabel("Kategorie") //
                .addTo(modalForm) //
                .addToGrid(formularGridPanel) //
                .getInputField();

        ModalField<TextBox, String> matrikelnummerField = newTextBoxField("matrikelnummer") //
                .withLabel("Matrikelnr.") //
                .addTo(modalForm);
        ModalField<TextBox, String> mitarbeiterNummerField = newTextBoxField("mitarbeiterNummer") //
                .withLabel("Mitarbeiternr.") //
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
            new Button(LocalizedString.Cancel.toString(), this::close) //
                    .setLayoutData(createLayoutData(BEGINNING, BEGINNING, true, false)) //
                    .addTo(lowerButtonPanel);
        }
        new Button("Reset", this::resetPersonenAngaben) //
                .setLayoutData(createLayoutData(BEGINNING, BEGINNING, true, false)) //
                .addTo(lowerButtonPanel);
        new Button("Save", this::savePersonenAngaben) //
                .setLayoutData(createLayoutData(END, BEGINNING, true, false)) //
                .addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()));
        return lowerButtonPanel.setLayoutData(createLayoutData(Fill));
    }


    private void resetPersonenAngaben() {
        setPersonenAngaben(new PersonenAngaben());
    }


    private void savePersonenAngaben() {
        BindingResult bindingResult = bindPersonenAngabenModalData();

        if (!bindingResult.hasErrors()) {
            this.personenAngaben = (PersonenAngaben) bindingResult.getTarget();
            this.close();
        }
    }


    private BindingResult bindPersonenAngabenModalData() {
        DataBinder dataBinder = new DataBinder(new PersonenAngaben());
        dataBinder.setAllowedFields("vorname", "nachname", "email", "telefon", "gender", "street", "ort",
                "personKategorie", "matrikelnummer", "mitarbeiterNummer", "iban", "kontoInhaber");
        dataBinder.addValidators(new PersonAngabenValidator());
        return modalForm.bindData(dataBinder);
    }


    public void setPersonenAngaben(PersonenAngaben personenAngaben) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(personenAngaben);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            propertyValues.addPropertyValue(propertyName, propertyValue);
        }
        modalForm.writePropertyValues(propertyValues);
    }


    public Optional<PersonenAngaben> getPersonenAngaben() {
        return Optional.ofNullable(personenAngaben);
    }


    @Override
    public void close() {
        applicationDataService.removePersonenAngabenListener(this);
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
        } else if (!forceValidPersonenAngaben && KeyType.Escape.equals(keyStroke.getKeyType())) {
            close();
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
    public void onChangedPersonenAngaben(PersonenAngaben changedPersonenAngaben) {
        setPersonenAngaben(changedPersonenAngaben);
    }


    @Override
    public Optional<PersonenAngaben> showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        return getPersonenAngaben();
    }

}
