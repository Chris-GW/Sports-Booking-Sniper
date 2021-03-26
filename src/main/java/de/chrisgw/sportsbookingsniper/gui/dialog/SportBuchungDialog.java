package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.buchung.strategie.SportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.gui.component.SearchableComboBox;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class SportBuchungDialog extends DialogWindow {

    private final ApplicationStateDao applicationStateDao;
    private final AtomicBoolean saved = new AtomicBoolean(false);

    private final Label sportArtLabel = new Label("SportArt:*");
    private final ComboBox<SportArt> sportArtComboBox = new SearchableComboBox<>();

    private final Label sportAngebotLabel = new Label("SportAngebot:*");
    private final ComboBox<SportAngebot> sportAngebotComboBox = new SearchableComboBox<>();

    private final Label sportTerminLabel = new Label("SportTermin:*");
    private final ComboBox<SportTermin> sportTerminComboBox = new ComboBox<>();

    private final Label teilnehmerLabel = new Label("Teilnehmer:*");
    private final ComboBox<Teilnehmer> teilnehmerComboBox = new ComboBox<>();


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
        addSportTerminComboBox(formularGridPanel);
        addTeilnehmerComboBox(formularGridPanel);
        return formularGridPanel;
    }


    private void addSportArtComboBox(Panel formularGridPanel) {
        sportArtLabel.addTo(formularGridPanel);
        SportKatalog sportKatalog = applicationStateDao.currentSportKatalog();
        sportKatalog.getSportArten().forEach(sportArtComboBox::addItem);
        sportArtComboBox.setSelectedItem(null);
        sportArtComboBox.addListener(onSelectSportArt());
        sportArtComboBox.setLayoutData(createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }

    private Listener onSelectSportArt() {
        return (selectedIndex, previousSelection, changedByUserInteraction) -> {
            sportAngebotComboBox.setEnabled(selectedIndex >= 0);
            sportAngebotComboBox.clearItems();
            SportArt sportArt = sportArtComboBox.getSelectedItem();
            if (sportArt != null) {
                sportArt.upcomingSportAngebote().forEach(sportAngebotComboBox::addItem);
                sportAngebotComboBox.setSelectedItem(null);
            }
        };
    }


    private void addSportAngebotComboBox(Panel formularGridPanel) {
        sportAngebotLabel.addTo(formularGridPanel);
        sportAngebotComboBox.setEnabled(false);
        sportAngebotComboBox.setDropDownNumberOfRows(19);
        sportAngebotComboBox.addListener(onSelectSportAngebot());
        sportAngebotComboBox.setPreferredSize(new TerminalSize(60, 1));
        sportAngebotComboBox.setLayoutData(createHorizontallyFilledLayoutData());
        sportAngebotComboBox.addTo(formularGridPanel);
    }

    private Listener onSelectSportAngebot() {
        return (selectedIndex, previousSelection, changedByUserInteraction) -> {
            sportTerminComboBox.setEnabled(selectedIndex >= 0);
            sportTerminComboBox.clearItems();
            SportAngebot sportAngebot = sportAngebotComboBox.getSelectedItem();
            if (sportAngebot != null) {
                sportAngebot.bevorstehendeSportTermine().forEachOrdered(sportTerminComboBox::addItem);
                sportTerminComboBox.setSelectedItem(null);
            }
        };
    }


    private void addSportTerminComboBox(Panel formularGridPanel) {
        sportTerminLabel.addTo(formularGridPanel);
        sportTerminComboBox.setEnabled(false);
        sportTerminComboBox.setDropDownNumberOfRows(18);
        sportTerminComboBox.setLayoutData(createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }


    private void addTeilnehmerComboBox(Panel formularGridPanel) {
        teilnehmerLabel.addTo(formularGridPanel);
        teilnehmerComboBox.setDropDownNumberOfRows(17);
        applicationStateDao.getTeilnehmerListe().forEach(teilnehmerComboBox::addItem);
        teilnehmerComboBox.setSelectedItem(applicationStateDao.getDefaultTeilnehmer());
        teilnehmerComboBox.setLayoutData(createHorizontallyFilledLayoutData()).addTo(formularGridPanel);
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        new Button("Zurücksetzen", this::resetSportBuchungsJob).addTo(lowerButtonPanel);
        new Button(LocalizedString.Save.toString(), this::saveSportBuchungsJob).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }

    public void resetSportBuchungsJob() {
        setSportBuchungsJob(null);
    }

    public void saveSportBuchungsJob() {
        if (validateSportBuchungsJob()) {
            saved.set(true);
            this.close();
        }
    }

    private boolean validateSportBuchungsJob() {
        boolean unselectedSportArt = isUnselectedComboBox(sportArtComboBox, sportArtLabel);
        boolean unselectedSportAngebot = isUnselectedComboBox(sportAngebotComboBox, sportAngebotLabel);
        boolean unselectedSportTermin = isUnselectedComboBox(sportTerminComboBox, sportTerminLabel);
        boolean unselectedTeilnehmer = isUnselectedComboBox(teilnehmerComboBox, teilnehmerLabel);
        return !(unselectedSportArt || unselectedSportAngebot || unselectedSportTermin || unselectedTeilnehmer);
    }

    private <T> boolean isUnselectedComboBox(ComboBox<T> comboBox, Label label) {
        boolean unselectedComboBox = comboBox.getSelectedItem() == null;
        if (label != null && unselectedComboBox) {
            label.setForegroundColor(ANSI.RED).setBackgroundColor(ANSI.WHITE);
        } else if (label != null) {
            label.setForegroundColor(null).setBackgroundColor(null);
        }
        return unselectedComboBox;
    }


    public SportBuchungsJob readSportBuchungsJob() {
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob();
        sportBuchungsJob.setSportAngebot(sportAngebotComboBox.getSelectedItem());
        sportBuchungsJob.setSportTermin(sportTerminComboBox.getSelectedItem());
        // TODO multi teilnehmer
        List<Teilnehmer> teilnehmerList = List.of(teilnehmerComboBox.getSelectedItem());
        sportBuchungsJob.setTeilnehmerListe(teilnehmerList);
        sportBuchungsJob.setSportAngebot(sportAngebotComboBox.getSelectedItem());
        // TODO configure buchungsStrategie
        SportBuchungsStrategie buchungsStrategie = KonfigurierbareSportBuchungsStrategie.defaultKonfiguration();
        sportBuchungsJob.setBuchungsWiederholungsStrategie(buchungsStrategie);
        return sportBuchungsJob;
    }

    public void setSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob == null) {
            sportArtComboBox.setSelectedItem(null);
            sportAngebotComboBox.setSelectedItem(null);
            sportTerminComboBox.setSelectedItem(null);
            teilnehmerComboBox.setSelectedItem(applicationStateDao.getDefaultTeilnehmer());
        } else {
            sportArtComboBox.setSelectedItem(sportBuchungsJob.getSportArt());
            sportAngebotComboBox.setSelectedItem(sportBuchungsJob.getSportAngebot());
            sportTerminComboBox.setSelectedItem(sportBuchungsJob.getSportTermin());
            Teilnehmer teilnehmer = sportBuchungsJob.getTeilnehmerListe().stream().findFirst().orElse(null);
            teilnehmerComboBox.setSelectedItem(teilnehmer);
        }
    }


    @Override
    public Optional<SportBuchungsJob> showDialog(WindowBasedTextGUI textGUI) {
        saved.set(false);
        super.showDialog(textGUI);
        if (saved.get()) {
            return Optional.of(readSportBuchungsJob());
        } else {
            return Optional.empty();
        }
    }

}
