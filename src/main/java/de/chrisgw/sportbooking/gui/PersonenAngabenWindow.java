package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportbooking.gui.bind.ComboBoxPropertyInput;
import de.chrisgw.sportbooking.gui.bind.PropertyInput;
import de.chrisgw.sportbooking.gui.bind.TextBoxPropertyInput;
import de.chrisgw.sportbooking.model.PersonAngabenValidator;
import de.chrisgw.sportbooking.model.PersonKategorie;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.model.PersonenAngaben.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static org.apache.commons.lang3.StringUtils.capitalize;


@Slf4j
public class PersonenAngabenWindow extends DialogWindow {

    private DataBinder dataBinder;
    private Map<String, Label> propertyInputLabels = new LinkedHashMap<>();
    private Map<String, PropertyInput> propertyInputs = new LinkedHashMap<>();

    private TerminalSize inputSize = new TerminalSize(35, 1);
    private Panel formularGridPanel;
    private Label focusedInformationLabel;


    public PersonenAngabenWindow() {
        super("Personenangaben");

        Panel contentPane = new Panel();
        createFormularPanel().addTo(contentPane);
        contentPane.addComponent(new EmptySpace());
        createLowerButtonPanel().addTo(contentPane);
        this.focusedInformationLabel = new Label("").addTo(contentPane);
        addBasePaneListener(basePanelListener());

        this.dataBinder = createDataBinder();
        setComponent(contentPane);
    }

    private BasePaneListener<Window> basePanelListener() {
        return new BasePaneListener<>() {

            @Override
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                log.debug("onInput {}", keyStroke);
            }

            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                log.debug("onUnhandledInput {}", keyStroke);
            }

        };
    }


    private Panel createFormularPanel() {
        formularGridPanel = new Panel(new GridLayout(2));

        createTextBoxInput("vorname");
        createTextBoxInput("nachname");
        createGenderComboBox();
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        createTextBoxInput("email");
        createTextBoxInput("telefon");
        createTextBoxInput("street", "Straße");
        createTextBoxInput("ort");
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        createPersonenKategorieComboBox();
        createTextBoxInput("matrikelnummer", "Matrikelnr.");
        createTextBoxInput("mitarbeiterNummer", "Mitarbeiternr.");
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        createTextBoxInput("iban", "IBAN");
        createTextBoxInput("kontoInhaber", "Kontoinhaber");
        return formularGridPanel;
    }

    private DataBinder createDataBinder() {
        DataBinder dataBinder = new DataBinder(new PersonenAngaben());
        dataBinder.setAllowedFields(propertyInputs.keySet().toArray(new String[0]));
        dataBinder.addValidators(new PersonAngabenValidator());
        return dataBinder;
    }


    private void createTextBoxInput(String propertyName) {
        createTextBoxInput(propertyName, capitalize(propertyName));
    }

    private void createTextBoxInput(String propertyName, String labelText) {
        propertyInputLabels.put(propertyName, new Label(labelText).addTo(formularGridPanel));
        TextBox textBox = new TextBox().setPreferredSize(inputSize).addTo(formularGridPanel);
        propertyInputs.put(propertyName, new TextBoxPropertyInput(propertyName, textBox));
    }


    private void createGenderComboBox() {
        String propertyName = "gender";
        propertyInputLabels.put(propertyName, new Label("Geschlecht").addTo(formularGridPanel));
        ComboBox<Gender> genderComboBox = new ComboBox<>(Gender.values()) //
                .setPreferredSize(inputSize) //
                .addTo(formularGridPanel);
        propertyInputs.put(propertyName, new ComboBoxPropertyInput<>(propertyName, genderComboBox));
    }


    private void createPersonenKategorieComboBox() {
        String propertyName = "personKategorie";
        propertyInputLabels.put(propertyName, new Label("Kategorie").addTo(formularGridPanel));
        ComboBox<PersonKategorie> personKategorieComboBox = new ComboBox<>(PersonKategorie.values()) //
                .setPreferredSize(inputSize) //
                .addListener(this::onPersonenKategorieChange) //
                .addTo(formularGridPanel);
        propertyInputs.put(propertyName, new ComboBoxPropertyInput<>(propertyName, personKategorieComboBox));
    }

    private void onPersonenKategorieChange(int selectedIndex, int previousSelection) {
        PersonKategorie selectedPersonKategorie = (PersonKategorie) propertyInputs.get("personKategorie")
                .getPropertyValue();
        getInputComponent("matrikelnummer", TextBox.class).setEnabled(selectedPersonKategorie.requiresMatrikelnummer());
        getInputComponent("mitarbeiterNummer", TextBox.class).setEnabled(
                selectedPersonKategorie.requiresMitarbeiterNummer());
    }


    private Panel createLowerButtonPanel() {
        Button closeBtn = new Button("Close", this::close);
        Button saveBtn = new Button("Save", this::savePersonenAngaben).setLayoutData(
                GridLayout.createLayoutData(Alignment.END, Alignment.BEGINNING, true, true));
        return Panels.horizontal(closeBtn, saveBtn);
    }


    private void savePersonenAngaben() {
        dataBinder.bind(readPropertyValues());
        dataBinder.validate();
        BindingResult bindingResult = dataBinder.getBindingResult();
        for (PropertyInput propertyInput : propertyInputs.values()) {
            setPropertyInputBindingResult(propertyInput, bindingResult);
        }

        if (!bindingResult.hasErrors()) {
            // TODO save
            System.out.println("save personenangaben");
            this.close();
        }
    }

    private void setPropertyInputBindingResult(PropertyInput propertyInput, BindingResult bindingResult) {
        propertyInput.setBindingResult(bindingResult);
        Label label = getInputLabel(propertyInput.getPropertyName());
        if (propertyInput.hasFieldErrors()) {
            label.setForegroundColor(ANSI.WHITE).setBackgroundColor(ANSI.RED);
        } else {
            label.setForegroundColor(null).setBackgroundColor(null);
        }
    }

    private PropertyValues readPropertyValues() {
        return propertyInputs.values()
                .stream()
                .map(PropertyInput::toPropertyValues)
                .collect(MutablePropertyValues::new, MutablePropertyValues::addPropertyValues,
                        MutablePropertyValues::addPropertyValues);
    }


    private Label getInputLabel(String propertyName) {
        return propertyInputLabels.get(propertyName);
    }

    private Interactable getInputComponent(String propertyName) {
        return getInputComponent(propertyName, Interactable.class);
    }

    private <T extends Interactable> T getInputComponent(String propertyName, Class<T> clazz) {
        return clazz.cast(propertyInputs.get(propertyName).getInputComponent());
    }


    public PersonenAngaben personenAngaben() {
        return (PersonenAngaben) dataBinder.getTarget();
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void bindPersonenAngaben(PersonenAngaben personenAngaben) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(personenAngaben);
        for (PropertyInput propertyInput : propertyInputs.values()) {
            Object propertyValue = beanWrapper.getPropertyValue(propertyInput.getPropertyName());
            propertyInput.setPropertyValue(propertyValue);
        }
    }

}
