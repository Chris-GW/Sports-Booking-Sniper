package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.repository.ApplicationStateDao;
import de.chrisgw.sportbooking.repository.ApplicationStateDao.SportBuchungJobListener;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class PendingSportBuchungenComponent extends SportBookingComponent implements SportBuchungJobListener {

    @Getter
    private final Table<String> pendingJobsTabel;


    public PendingSportBuchungenComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "Ausstehende SportBuchungen", KeyType.F2);
        setLayoutManager(new BorderLayout());

        this.pendingJobsTabel = createPendingJobsTabel();
        this.applicationStateDao.getPendingBuchungsJobs().forEach(this::addPendingJob);

        addComponent(pendingJobsTabel, Location.CENTER);
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


    private Table<String> createPendingJobsTabel() {
        Table<String> pendingJobsTabel = new Table<>("#", "Sportangebot", "Details");
        pendingJobsTabel.setVisibleRows(6);
        pendingJobsTabel.setVisibleColumns(3);
        pendingJobsTabel.setSelectAction(this::onSelectPendingJob);
        return pendingJobsTabel;
    }

    private void onSelectPendingJob() {
        int selectedRow = pendingJobsTabel.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= applicationStateDao.getPendingBuchungsJobs().size()) {
            return;
        }
        SportBuchungsJob sportBuchungsJob = applicationStateDao.getPendingBuchungsJobs().get(selectedRow);

        new ActionListDialogBuilder().setTitle("Beendete Sportbuchung")
                .setDescription("Aktion bitte auswählen")
                .setCanCancel(true)
                .addAction("Details", () -> {
                    System.out.println("Details: " + sportBuchungsJob);
                })
                .addAction("Löschen", () -> {
                    System.out.println("Löschen: " + sportBuchungsJob);
                })
                .build()
                .showDialog(getTextGUI());
    }


    private void addPendingJob(SportBuchungsJob sportBuchungsJob) {
        List<String> rowValues = new ArrayList<>();
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();
        SportAngebot sportAngebot = sportTermin.getSportAngebot();
        String kursnummer = sportAngebot.getKursnummer();
        String sportArtName = sportAngebot.getSportArt().getName();
        LocalDateTime timestamp = sportBuchungsJob.lastSportBuchungsVersuch().getTimestamp();
        String formatBuchungsTimestamp = DateTimeFormatter.ofPattern("dd.MM. HH:mm").format(timestamp);

        rowValues.add(String.valueOf(sportBuchungsJob.getJobId()));
        rowValues.add(String.format("%s - %s%n%s ", kursnummer, sportArtName, formatSportTermin(sportTermin)));
        rowValues.add(sportAngebot.getDetails() + "\nGebucht am " + formatBuchungsTimestamp + " ");
        pendingJobsTabel.getTableModel().addRow(rowValues);
    }


    private static String formatSportTermin(SportTermin sportTermin) {
        String startZeit = DateTimeFormatter.ofPattern("ccc dd.MM. HH:mm").format(sportTermin.getStartZeit());
        String endZeit = DateTimeFormatter.ofPattern("HH:mm").format(sportTermin.getEndZeit());
        return startZeit + "-" + endZeit;
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        addPendingJob(sportBuchungsJob);
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
