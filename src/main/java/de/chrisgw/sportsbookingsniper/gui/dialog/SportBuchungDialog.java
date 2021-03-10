package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.component.SearchableComboBox;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.util.Arrays;
import java.util.Optional;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class SportBuchungDialog extends DialogWindow {

    private final ApplicationStateDao applicationStateDao;

    private SportBuchungsJob sportBuchungsJob;

    private ComboBox<SportArt> sportArtComboBox;
    private ComboBox<SportAngebot> sportAngebotComboBox;
    private ComboBox<Object> terminComboBox;


    public SportBuchungDialog(ApplicationStateDao applicationStateDao) {
        super("Sportbuchung");
        this.applicationStateDao = applicationStateDao;
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setCloseWindowWithEscape(true);
        setComponent(createContentPane());
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
        SportKatalog sportKatalog = applicationStateDao.currentSportKatalog();
        sportArtComboBox = new SearchableComboBox<>(sportKatalog.getSportArten());
        sportArtComboBox.addListener(onSelectSportArt());
        sportArtComboBox.setLayoutData(createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }

    private Listener onSelectSportArt() {
        return (selectedIndex, previousSelection, changedByUserInteraction) -> {
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
        sportAngebotComboBox.setLayoutData(createHorizontallyFilledLayoutData());
        sportAngebotComboBox.addTo(formularGridPanel);
    }

    private Listener onSelectSportAngebot() {
        return (selectedIndex, previousSelection, changedByUserInteraction) -> {
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
        terminComboBox.setLayoutData(createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        new Button("Reset", this::resetSportBuchungsJob).addTo(lowerButtonPanel);
        new Button("Save", this::saveSportBuchungsJob).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }


    public void saveSportBuchungsJob() {
        // TODO saveSportBuchungsJob
    }


    public void setSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob == null) {
            throw new IllegalArgumentException("Expect non null SportBuchungsJob");
        }
        // TODO setSportBuchungsJob
    }

    public void resetSportBuchungsJob() {
        setSportBuchungsJob(new SportBuchungsJob());
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
