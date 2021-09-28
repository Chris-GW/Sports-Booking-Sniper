package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.buchung.strategie.SportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.gui.component.FormPanel;
import de.chrisgw.sportsbookingsniper.gui.component.SearchableComboBox;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import lombok.Getter;

import java.util.List;


public class SportBuchungForm extends FormPanel<SportBuchungsJob> {

    private final ApplicationStateDao applicationStateDao;

    private final ComboBox<SportArt> sportArtComboBox = new SearchableComboBox<>();
    private final ComboBox<SportAngebot> sportAngebotComboBox = new SearchableComboBox<>();
    private final ComboBox<SportTermin> sportTerminComboBox = new ComboBox<>();
    private final CheckBoxList<Teilnehmer> teilnehmerComboBox = new CheckBoxList<>();

    @Getter
    private int jobId;


    public SportBuchungForm(ApplicationStateDao applicationStateDao) {
        super();
        this.applicationStateDao = applicationStateDao;

        addSportArtComboBox();
        addSportAngebotComboBox();
        addSportTerminComboBox();
        addTeilnehmerComboBox();
    }


    private void addSportArtComboBox() {
        SportKatalog sportKatalog = applicationStateDao.currentSportKatalog();
        sportKatalog.getSportArten().forEach(sportArtComboBox::addItem);
        sportArtComboBox.setSelectedItem(null);
        sportArtComboBox.addListener(onSelectSportArt());
        addFormularField("SportArt:*", sportArtComboBox);
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


    private void addSportAngebotComboBox() {
        sportAngebotComboBox.setEnabled(false);
        sportAngebotComboBox.setDropDownNumberOfRows(19);
        sportAngebotComboBox.addListener(onSelectSportAngebot());
        sportAngebotComboBox.setPreferredSize(new TerminalSize(60, 1));
        addFormularField("SportAngebot:*", sportAngebotComboBox);
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


    private void addSportTerminComboBox() {
        sportTerminComboBox.setEnabled(false);
        sportTerminComboBox.setDropDownNumberOfRows(18);
        addFormularField("SportTermin:*", sportTerminComboBox);
    }


    private void addTeilnehmerComboBox() {
        applicationStateDao.getTeilnehmerListe().forEach(teilnehmerComboBox::addItem);
        teilnehmerComboBox.setChecked(applicationStateDao.getDefaultTeilnehmer(), true);
        addFormularField("Teilnehmer:*", teilnehmerComboBox);
    }


    @Override
    public boolean validateForm() {
        return List.of( //
                isUnselected(sportArtComboBox), //
                isUnselected(sportAngebotComboBox), //
                isUnselected(sportTerminComboBox), //
                isUnselected(teilnehmerComboBox) //
        ).contains(true);
    }


    @Override
    public SportBuchungsJob readFormValue() {
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob();
        sportBuchungsJob.setSportAngebot(sportAngebotComboBox.getSelectedItem());
        sportBuchungsJob.setSportTermin(sportTerminComboBox.getSelectedItem());
        List<Teilnehmer> teilnehmerList = teilnehmerComboBox.getCheckedItems();
        sportBuchungsJob.setTeilnehmerListe(teilnehmerList);
        sportBuchungsJob.setSportAngebot(sportAngebotComboBox.getSelectedItem());
        // TODO configure buchungsStrategie
        SportBuchungsStrategie buchungsStrategie = KonfigurierbareSportBuchungsStrategie.defaultKonfiguration();
        sportBuchungsJob.setBuchungsWiederholungsStrategie(buchungsStrategie);
        return sportBuchungsJob;
    }


    @Override
    public void setFormValue(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob == null) {
            jobId = 0;
            sportArtComboBox.setSelectedItem(null);
            sportAngebotComboBox.setSelectedItem(null);
            sportTerminComboBox.setSelectedItem(null);
            teilnehmerComboBox.clearItems().setChecked(applicationStateDao.getDefaultTeilnehmer(), true);
        } else {
            jobId = sportBuchungsJob.getJobId();
            sportArtComboBox.setSelectedItem(sportBuchungsJob.getSportArt());
            sportAngebotComboBox.setSelectedItem(sportBuchungsJob.getSportAngebot());
            sportTerminComboBox.setSelectedItem(sportBuchungsJob.getSportTermin());

            teilnehmerComboBox.clearItems();
            for (Teilnehmer teilnehmer : sportBuchungsJob.getTeilnehmerListe()) {
                teilnehmerComboBox.setChecked(teilnehmer, true);
            }
        }
    }

}
