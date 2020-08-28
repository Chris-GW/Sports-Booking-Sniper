package de.chrisgw.sportbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.angebot.SportArt;
import de.chrisgw.sportbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngabenValidator;
import de.chrisgw.sportbookingsniper.gui.bind.ModalForm;
import de.chrisgw.sportbookingsniper.gui.component.SearchableComboBox;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.angebot.SportKatalogRepository;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Optional;

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class SportBuchungDialog extends DialogWindow {

    private final SportKatalogRepository sportKatalogRepository;

    private SportBuchungsJob sportBuchungsJob;
    private ModalForm modalForm = new ModalForm();

    private ComboBox<SportArt> sportArtComboBox;
    private ComboBox<SportAngebot> sportAngebotComboBox;
    private ComboBox<Object> terminComboBox;


    public SportBuchungDialog(SportKatalogRepository sportKatalogRepository) {
        super("Sportbuchung");
        this.sportKatalogRepository = sportKatalogRepository;
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
                GridLayout.createHorizontallyFilledLayoutData(2));
        formularGridPanel.addComponent(new EmptySpace(), GridLayout.createHorizontallyFilledLayoutData(2));

        addSportArtComboBox(formularGridPanel);
        addSportAngebotComboBox(formularGridPanel);
        addTerminComboBox(formularGridPanel);
        return formularGridPanel;
    }


    private void addSportArtComboBox(Panel formularGridPanel) {
        new Label("SportArt:*").addTo(formularGridPanel);
        SportKatalog sportKatalog = sportKatalogRepository.findCurrentSportKatalog();
        sportArtComboBox = new SearchableComboBox<>(sportKatalog.getSportArten());
        sportArtComboBox.addListener(onSelectSportArt());
        sportArtComboBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }

    private Listener onSelectSportArt() {
        return (selectedIndex, previousSelection) -> {
            sportAngebotComboBox.setEnabled(true);
            sportAngebotComboBox.clearItems();
            SportArt sportArt = sportArtComboBox.getSelectedItem();
            if (sportArt != null) {
                sportArt.upcomingSportAngebote().forEach(sportAngebotComboBox::addItem);
            }
        };
    }

    private void addSportAngebotComboBox(Panel formularGridPanel) {
        new Label("SportAngebot:*").addTo(formularGridPanel);
        sportAngebotComboBox = new SearchableComboBox<>();
        sportAngebotComboBox.setEnabled(false);
        sportAngebotComboBox.setDropDownNumberOfRows(19);
        sportAngebotComboBox.addListener(onSelectSportAngebot());
        sportAngebotComboBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData());
        sportAngebotComboBox.addTo(formularGridPanel);
    }

    private Listener onSelectSportAngebot() {
        return (selectedIndex, previousSelection) -> {
            terminComboBox.setEnabled(true);
            terminComboBox.clearItems();
            SportAngebot sportAngebot = sportAngebotComboBox.getSelectedItem();
            if (sportAngebot != null) {
                sportAngebot.bevorstehendeSportTermine().forEachOrdered(terminComboBox::addItem);
            }
        };
    }


    private void addTerminComboBox(Panel formularGridPanel) {
        new Label("SportTermin:*").addTo(formularGridPanel);
        terminComboBox = new ComboBox<>();
        terminComboBox.setEnabled(false);
        terminComboBox.setDropDownNumberOfRows(18);
        terminComboBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
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
        DataBinder dataBinder = new DataBinder(new TeilnehmerAngaben());
        dataBinder.setAllowedFields("TODO");
        dataBinder.addValidators(new TeilnehmerAngabenValidator());
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
