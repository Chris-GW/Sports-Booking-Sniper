package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportbooking.gui.bind.ModalForm;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.SportBookingService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Optional;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class SportBuchungDialog extends DialogWindow {

    private final SportBookingService sportBookingService;

    private SportBuchungsJob sportBuchungsJob;
    private ModalForm modalForm = new ModalForm();

    private ComboBox<SportArt> sportArtComboBox;
    private ComboBox<SportAngebot> sportAngebotComboBox;
    private ComboBox<Object> terminComboBox;


    public SportBuchungDialog(SportBookingService sportBookingService) {
        super("Sportbuchung");
        this.sportBookingService = sportBookingService;
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setComponent(createContentPane());
        setCloseWindowWithEscape(true);
    }


    private Panel createContentPane() {
        Panel contentPane = new Panel();
        contentPane.addComponent(createFormularPanel());
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel(), createLayoutData(End));
        return contentPane;
    }

    private Panel createFormularPanel() {
        Panel formularGridPanel = new Panel(new GridLayout(2));
        formularGridPanel.addComponent(new Label("Bitte wählen Sie das zu buchende SportAngebot aus:"),
                createHorizontallyFilledLayoutData(2));
        formularGridPanel.addComponent(new EmptySpace(), createHorizontallyFilledLayoutData(2));

        addSportArtComboBox(formularGridPanel);
        addSportAngebotComboBox(formularGridPanel);
        addTerminComboBox(formularGridPanel);
        return formularGridPanel;
    }


    private void addSportArtComboBox(Panel formularGridPanel) {
        new Label("SportArt:*").addTo(formularGridPanel);
        SportKatalog sportKatalog = sportBookingService.loadSportKatalog();
        this.sportArtComboBox = new SearchableComboBox<>(sportKatalog.getSportArten()) //
                .addListener(onSelectSportArt()) //
                .addTo(formularGridPanel);
    }

    private Listener onSelectSportArt() {
        return (selectedIndex, previousSelection) -> {
            SportArt sportArt = sportArtComboBox.getSelectedItem();
            sportAngebotComboBox.setEnabled(true);
            sportAngebotComboBox.clearItems();
            sportArt.upcomingSportAngebote().forEach(sportAngebotComboBox::addItem);
        };
    }

    private void addSportAngebotComboBox(Panel formularGridPanel) {
        new Label("SportAngebot:*").addTo(formularGridPanel);
        sportAngebotComboBox = new SearchableComboBox<>();
        sportAngebotComboBox.setEnabled(false);
        sportAngebotComboBox.setDropDownNumberOfRows(19);
        sportAngebotComboBox.addListener(onSelectSportAngebot());
        sportAngebotComboBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)).addTo(formularGridPanel);
    }

    private Listener onSelectSportAngebot() {
        return (selectedIndex, previousSelection) -> {
            SportAngebot sportAngebot = sportAngebotComboBox.getSelectedItem();
            terminComboBox.setEnabled(true);
            terminComboBox.clearItems();
            sportAngebot.upcomingSportTermine().forEachOrdered(terminComboBox::addItem);
        };
    }


    private void addTerminComboBox(Panel formularGridPanel) {
        new Label("SportTermin:*").addTo(formularGridPanel);
        terminComboBox = new ComboBox<>();
        terminComboBox.setEnabled(false);
        terminComboBox.setDropDownNumberOfRows(18);
        terminComboBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)).addTo(formularGridPanel);
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        new Button("Reset", this::resetSportBuchungsJob).addTo(lowerButtonPanel);
        new Button("Save", this::saveSportBuchungsJob).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }


    private void resetSportBuchungsJob() {
        setSportBuchungsJob(new SportBuchungsJob());
    }


    private void saveSportBuchungsJob() {
        BindingResult bindingResult = bindPersonenAngabenModalData();

        if (!bindingResult.hasErrors()) {
            this.sportBuchungsJob = (SportBuchungsJob) bindingResult.getTarget();
            this.close();
        }
    }


    private BindingResult bindPersonenAngabenModalData() {
        DataBinder dataBinder = new DataBinder(new PersonenAngaben());
        dataBinder.setAllowedFields("TODO");
        dataBinder.addValidators(new PersonAngabenValidator());
        return modalForm.bindData(dataBinder);
    }


    public void setSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(sportBuchungsJob);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            propertyValues.addPropertyValue(propertyName, propertyValue);
        }
        modalForm.writePropertyValues(propertyValues);
    }


    public Optional<SportBuchungsJob> getSportBuchungsJob() {
        return Optional.ofNullable(sportBuchungsJob);
    }


    @Override
    public Optional<SportBuchungsJob> showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        return getSportBuchungsJob();
    }

}
