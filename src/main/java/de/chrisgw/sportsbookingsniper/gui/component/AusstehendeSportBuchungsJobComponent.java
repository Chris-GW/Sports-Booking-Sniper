package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsSniperService;
import de.chrisgw.sportsbookingsniper.gui.dialog.SportBuchungDialog;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;

import static java.util.Objects.requireNonNull;


public class AusstehendeSportBuchungsJobComponent extends BasicPanelComponent implements SportBuchungsJobListener {

    private SportBuchungsSniperService sniperService;
    private SportBuchungsJobTable sportBuchungsJobTable = new SportBuchungsJobTable();
    private ShortKeyRegistry shortKeyRegistry = new ShortKeyRegistry();


    public AusstehendeSportBuchungsJobComponent(ApplicationStateDao applicationStateDao,
            SportBuchungsSniperService sniperService, Window window) {
        super(applicationStateDao, window, "Ausstehende SportBuchungen", KeyType.F2);
        this.sniperService = requireNonNull(sniperService);
        setLayoutManager(new LinearLayout(Direction.VERTICAL));
        addComponent(sportBuchungsJobTable);
        window.addWindowListener(shortKeyRegistry);

        shortKeyRegistry.registerAction(new KeyStroke('b', true, false), this::editSportBuchungsJob);
        shortKeyRegistry.registerAction(new KeyStroke(KeyType.F5), this::retrySportBuchungsJob);
        shortKeyRegistry.registerAction(new KeyStroke('c', true, false), this::copySportBuchungsJob);
        shortKeyRegistry.registerAction(new KeyStroke(KeyType.Delete), this::deleteSelectedSportBuchungsJob);
        shortKeyRegistry.registerAction(new KeyStroke(' ', true, false), this::togglePauseOfSelectedSportBuchungsJob);
        sportBuchungsJobTable.setSelectAction(this::showSportBuchungsJobContextMenu);
        applicationStateDao.getPendingBuchungsJobs().forEach(this::addSportBuchungsJob);
    }


    private void showSportBuchungsJobContextMenu() {
        SportBuchungsJob buchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        SportTermin sportTermin = buchungsJob.getSportTermin();
        String togglePauseLabel;
        if (buchungsJob.isPausiert()) {
            togglePauseLabel = "Fortsetzen <c-Space>";
        } else {
            togglePauseLabel = "Pausieren <c-Space>";
        }

        new ActionListDialogBuilder() //
                .setTitle("Ausgewählter Sport Buchungs Job ...")
                .setDescription(sportAngebot.getName() + "\n" + sportTermin.toString())
                .setCanCancel(true)
                .setCloseAutomaticallyOnAction(true)
                .addAction("Bearbeiten <c-b>", this::editSportBuchungsJob)
                .addAction("Jetzt Versuchen zu buchen <F5>", this::retrySportBuchungsJob)
                .addAction("Buchung kopieren <S-c>", this::copySportBuchungsJob)
                .addAction("Löschen <Entf>", this::deleteSelectedSportBuchungsJob)
                .addAction(togglePauseLabel, this::togglePauseOfSelectedSportBuchungsJob)
                .build()
                .showDialog(getTextGUI());
    }

    private void editSportBuchungsJob() {
        SportBuchungsJob buchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        if (buchungsJob != null && sportBuchungsJobTable.isFocused()) {
            new SportBuchungDialog(null).showDialog(getTextGUI());
        }
    }


    private void copySportBuchungsJob() {
        // TODO copySportBuchungsJob
        SportBuchungsJob selectedSportBuchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        if (selectedSportBuchungsJob != null && sportBuchungsJobTable.isFocused()) {

        }
    }

    private void retrySportBuchungsJob() {
        SportBuchungsJob buchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        if (buchungsJob != null && sportBuchungsJobTable.isFocused()) {
            sniperService.cancelSportBuchungsJob(buchungsJob);
            sniperService.submitSportBuchungsJob(buchungsJob);
        }
    }

    private void deleteSelectedSportBuchungsJob() {
        SportBuchungsJob selectedSportBuchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        if (selectedSportBuchungsJob != null && sportBuchungsJobTable.isFocused()) {
            removeSportBuchungsJob(selectedSportBuchungsJob);
        }
    }


    private void togglePauseOfSelectedSportBuchungsJob() {
        SportBuchungsJob buchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
        if (buchungsJob != null && sportBuchungsJobTable.isFocused()) {
            buchungsJob.setPausiert(!buchungsJob.isPausiert());
            sniperService.cancelSportBuchungsJob(buchungsJob);
        }
    }



    public void setVisibleRows(int visibleRows) {
        //        sportBuchungsJobTable.setVisibleRows(visibleRows);
    }


    public void addSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        sportBuchungsJobTable.addSportBuchungsJob(sportBuchungsJob);
    }

    public void removeSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        sportBuchungsJobTable.removeSportBuchungsJob(sportBuchungsJob);
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob newBuchungsJob) {
        addSportBuchungsJob(newBuchungsJob);
    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        invalidate();
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        removeSportBuchungsJob(sportBuchungsJob);
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addSportBuchungsJobListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeSportBuchungsJobListener(this);
    }


    @Override
    protected AusstehendeSportBuchungsJobComponent self() {
        return this;
    }

}
