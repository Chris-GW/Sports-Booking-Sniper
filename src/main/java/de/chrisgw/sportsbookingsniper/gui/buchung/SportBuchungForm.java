package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
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
import de.chrisgw.sportsbookingsniper.gui.state.TeilnehmerListeListener;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static java.util.stream.Collectors.toList;


public class SportBuchungForm extends FormPanel<SportBuchungsJob> implements TeilnehmerListeListener {

    private final ApplicationStateDao applicationStateDao;

    private final ComboBox<SportArt> sportArtComboBox = new SearchableComboBox<>();
    private final ComboBox<SportAngebot> sportAngebotComboBox = new SearchableComboBox<>();
    private final ComboBox<SportTermin> sportTerminComboBox = new ComboBox<>();
    private final CheckBoxList<Teilnehmer> teilnehmerComboBox = new CheckBoxList<>();
    private final Label noSportTermineLabel = new Label("");

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
                ForkJoinPool.commonPool().execute(() -> {
                    Set<SportAngebot> sportAngebote = sportArt.getSportAngebote();
                    runOnGUIThreadIfExistsOtherwiseRunDirect(() -> {
                        sportAngebote.forEach(sportAngebotComboBox::addItem);
                        sportAngebotComboBox.setSelectedItem(null);
                    });
                });
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
                ForkJoinPool.commonPool().execute(() -> {
                    List<SportTermin> sportTermine = sportAngebot.bevorstehendeSportTermine().collect(toList());
                    runOnGUIThreadIfExistsOtherwiseRunDirect(() -> {
                        sportTermine.forEach(sportTerminComboBox::addItem);
                        sportTerminComboBox.setSelectedItem(null);

                        boolean noAvailableTermine = sportTerminComboBox.getItemCount() == 0;
                        noSportTermineLabel.setVisible(noAvailableTermine);
                        setFieldFeedback(sportTerminComboBox, noAvailableTermine);
                        sportTerminComboBox.setEnabled(!noAvailableTermine);
                    });
                });
            }
        };
    }


    private void addSportTerminComboBox() {
        sportTerminComboBox.setEnabled(false);
        sportTerminComboBox.setDropDownNumberOfRows(18);
        addFormularField("SportTermin:*", sportTerminComboBox);

        noSportTermineLabel.setText("Ausgewähltes Sport Angebot hat leider keine buchbaren Termine");
        noSportTermineLabel.setVisible(false);
        noSportTermineLabel.setLayoutData(LinearLayout.createLayoutData(Alignment.End));
        noSportTermineLabel.setForegroundColor(ANSI.RED).setBackgroundColor(ANSI.WHITE);
        addFormularComponent(noSportTermineLabel);
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
            Teilnehmer defaultTeilnehmer = applicationStateDao.getDefaultTeilnehmer();
            uncheckAllTeilnehmer().setChecked(defaultTeilnehmer, true);
        } else {
            jobId = sportBuchungsJob.getJobId();
            sportArtComboBox.setSelectedItem(sportBuchungsJob.getSportArt());
            sportAngebotComboBox.setSelectedItem(sportBuchungsJob.getSportAngebot());
            sportTerminComboBox.setSelectedItem(sportBuchungsJob.getSportTermin());

            uncheckAllTeilnehmer();
            for (Teilnehmer teilnehmer : sportBuchungsJob.getTeilnehmerListe()) {
                teilnehmerComboBox.setChecked(teilnehmer, true);
            }
        }
    }

    private CheckBoxList<Teilnehmer> uncheckAllTeilnehmer() {
        teilnehmerComboBox.getItems().forEach(checkedItem -> teilnehmerComboBox.setChecked(checkedItem, false));
        return teilnehmerComboBox;
    }


    @Override
    public void onChangedTeilnehmerListe(List<Teilnehmer> changedTeilnehmerListe) {
        List<Teilnehmer> checkedTeilnehmer = teilnehmerComboBox.getCheckedItems();
        teilnehmerComboBox.clearItems();
        for (Teilnehmer teilnehmer : changedTeilnehmerListe) {
            boolean checkedState = checkedTeilnehmer.contains(teilnehmer);
            teilnehmerComboBox.addItem(teilnehmer, checkedState);
        }
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addTeilnehmerListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeTeilnehmerListener(this);
    }

}
