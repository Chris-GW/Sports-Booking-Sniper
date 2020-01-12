package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportbooking.gui.bind.ConcealableComponent;
import de.chrisgw.sportbooking.gui.bind.ModalField;
import de.chrisgw.sportbooking.gui.bind.ModalForm;
import de.chrisgw.sportbooking.model.PersonAngabenValidator;
import de.chrisgw.sportbooking.model.PersonKategorie;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.model.PersonenAngaben.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.beans.PropertyDescriptor;

import static com.googlecode.lanterna.gui2.Borders.doubleLine;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.*;
import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static de.chrisgw.sportbooking.gui.bind.ModalFieldBuilder.newComboBoxField;
import static de.chrisgw.sportbooking.gui.bind.ModalFieldBuilder.newTextBoxField;


@Slf4j
public class PersonenAngabenWindow extends DialogWindow {


    private ModalForm modalForm = new ModalForm();


    public PersonenAngabenWindow() {
        super("Personenangaben");
        createContentPane();
    }


    private void createContentPane() {
        Panel contentPane = new Panel();
        createFormularPanel().addTo(contentPane);
        createLowerButtonPanel().withBorder(doubleLine()).addTo(contentPane);
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
        Button closeBtn = new Button("Cancel", this::close).setLayoutData(
                GridLayout.createLayoutData(BEGINNING, BEGINNING, true, false));
        Button restBtn = new Button("Reset", this::resetPersonenAngaben).setLayoutData(
                GridLayout.createLayoutData(CENTER, BEGINNING, true, false));
        Button saveBtn = new Button("Save", this::savePersonenAngaben).setLayoutData(
                GridLayout.createLayoutData(END, BEGINNING, true, false));
        return Panels.grid(3, closeBtn, restBtn, saveBtn).setLayoutData(LinearLayout.createLayoutData(Fill));
    }


    private void resetPersonenAngaben() {
        bindPersonenAngaben(new PersonenAngaben());
    }


    private void savePersonenAngaben() {
        BindingResult bindingResult = bindModalData();

        if (!bindingResult.hasErrors()) {
            // TODO savePersonenAngaben
            System.out.println("save personenangaben");
            this.close();
        }
    }


    public PersonenAngaben personenAngaben() {
        return (PersonenAngaben) bindModalData().getTarget();
    }


    private BindingResult bindModalData() {
        DataBinder dataBinder = new DataBinder(new PersonenAngaben());
        dataBinder.addValidators(new PersonAngabenValidator());
        return modalForm.bindData(dataBinder);
    }


    public void bindPersonenAngaben(PersonenAngaben personenAngaben) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(personenAngaben);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            propertyValues.addPropertyValue(propertyName, propertyValue);
        }
        modalForm.writePropertyValues(propertyValues);
    }


}
