package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportbookingsniper.gui.state.SportBuchungJobListener;
import lombok.Getter;


public class PendingSportBuchungenComponent extends MainWindowBasicComponent implements SportBuchungJobListener {

    @Getter
    private final SportBuchungsJobListBox buchungsJobListBox;


    public PendingSportBuchungenComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "Ausstehende SportBuchungen", KeyType.F2);
        setLayoutManager(new LinearLayout(Direction.VERTICAL));

        buchungsJobListBox = new SportBuchungsJobListBox(this::showSportBuchungsJobContextMenu);
        addComponent(buchungsJobListBox);

        applicationStateDao.getPendingBuchungsJobs().forEach(buchungsJobListBox::onNewPendingSportBuchungsJob);
    }



    private void showSportBuchungsJobContextMenu(SportBuchungsJobListBox sportBuchungsJobListBox,
            SportBuchungsJob selectedBuchungsJob) {
        SportAngebot sportAngebot = selectedBuchungsJob.getSportAngebot();
        String sportArtName = sportAngebot.getSportArt().getName();
        String kursnummer = sportAngebot.getKursnummer();
        String formatTerminZeitraum = selectedBuchungsJob.getSportTermin().formatTerminZeitraum();
        new ActionListDialogBuilder().setTitle("Ausgewählte Sportbuchung...")
                .setDescription(sportArtName + "\n(" + kursnummer + ") " + formatTerminZeitraum)
                .setCanCancel(true)
                .setCloseAutomaticallyOnAction(true)
                .addAction("Bearbeiten", () -> {
                    System.out.println("Bearbeiten: " + selectedBuchungsJob);
                })
                .addAction("Pausieren <Pause>", () -> {
                    System.out.println("Pausieren: " + selectedBuchungsJob);
                })
                .addAction("Löschen <Entf>", () -> {
                    System.out.println("Löschen: " + selectedBuchungsJob);
                })
                .build()
                .showDialog(getTextGUI());
    }

    public void setVisibleRows(int visibleRows) {
        TerminalSize explicitPreferredSize = buchungsJobListBox.getPreferredSize().withRows(visibleRows);
        buchungsJobListBox.setPreferredSize(explicitPreferredSize);
    }

    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addSportBuchungJobListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeSportBuchungJobListener(this);
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        buchungsJobListBox.onNewPendingSportBuchungsJob(sportBuchungsJob);
    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        // TODO refreshPendingJob
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        // TODO refreshPendingJob
    }


    @Override
    protected PendingSportBuchungenComponent self() {
        return this;
    }

}
