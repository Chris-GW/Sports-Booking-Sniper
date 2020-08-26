package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsJob;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.repository.ApplicationStateDao;
import de.chrisgw.sportbooking.repository.ApplicationStateDao.SportBuchungJobListener;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class FinishedSportBuchungenComponent extends SportBookingComponent implements SportBuchungJobListener {

    @Getter
    private final Table<String> finishedJobsTabel;


    public FinishedSportBuchungenComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "Beendete SportBuchungen", KeyType.F3);
        setLayoutManager(new BorderLayout());

        this.finishedJobsTabel = createFinishedJobsTable();
        this.applicationStateDao.getFinishedBuchungsJobs().forEach(this::addFinishedBuchungsJob);

        addComponent(finishedJobsTabel, Location.CENTER);
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


    private Table<String> createFinishedJobsTable() {
        Table<String> finishedJobsTabel = new Table<>("#", "Sportangebot", "Details");
        finishedJobsTabel.setVisibleRows(6);
        finishedJobsTabel.setVisibleColumns(3);
        finishedJobsTabel.setSelectAction(this::onSelectFinishedJob);
        return finishedJobsTabel;
    }


    private void onSelectFinishedJob() {
        int selectedRow = finishedJobsTabel.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= applicationStateDao.getFinishedBuchungsJobs().size()) {
            return;
        }
        SportBuchungsJob selectedBuchungsJob = applicationStateDao.getFinishedBuchungsJobs().get(selectedRow);

        new ActionListDialogBuilder().setTitle("Beendete Sportbuchung")
                .setDescription("Aktion bitte auswählen")
                .setCanCancel(true)
                .addAction("Details", () -> {
                    // TODO action show details
                    System.out.println("Details: " + selectedBuchungsJob);
                })
                .addAction("Löschen", () -> {
                    // TODO action löschen
                    System.out.println("Löschen: " + selectedBuchungsJob);
                })
                .build()
                .showDialog(getTextGUI());
    }


    private void addFinishedBuchungsJob(SportBuchungsJob sportBuchungsJob) {
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
        finishedJobsTabel.getTableModel().addRow(rowValues);
    }


    private static String formatSportTermin(SportTermin sportTermin) {
        String startZeit = DateTimeFormatter.ofPattern("ccc dd.MM. HH:mm").format(sportTermin.getStartZeit());
        String endZeit = DateTimeFormatter.ofPattern("HH:mm").format(sportTermin.getEndZeit());
        return startZeit + "-" + endZeit;
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {

    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {

    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        addFinishedBuchungsJob(sportBuchungsJob);
    }


    @Override
    protected FinishedSportBuchungenComponent self() {
        return this;
    }


}
