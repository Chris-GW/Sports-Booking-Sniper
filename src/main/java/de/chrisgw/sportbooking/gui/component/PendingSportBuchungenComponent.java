package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsJob;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.ApplicationStateDao.SportBuchungJobListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class PendingSportBuchungenComponent extends Panel implements SportBuchungJobListener {

    private final ApplicationStateDao applicationStateDao;
    private final Table<String> pendingJobsTabel;


    public PendingSportBuchungenComponent(ApplicationStateDao applicationStateDao) {
        super(new BorderLayout());
        this.applicationStateDao = applicationStateDao;
        this.applicationStateDao.addSportBuchungJobListener(this);

        this.pendingJobsTabel = createPendingJobsTabel();
        getPendingBuchungsJobs().forEach(this::addPendingJob);

        addComponent(pendingJobsTabel, Location.CENTER);
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
        if (selectedRow < 0) {
            return;
        }
        SportBuchungsJob sportBuchungsJob = getPendingBuchungsJobs().get(selectedRow);

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
        LocalDateTime timestamp = sportBuchungsJob.getTimestamp();
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
    protected void onAfterDrawing(TextGUIGraphics graphics) {
        graphics.putString(TerminalPosition.TOP_LEFT_CORNER,
                String.format("pref = %s, size = %s, vis. rows = %d", getPreferredSize(), getSize(),
                        pendingJobsTabel.getVisibleRows()));
    }


    public Table<String> getPendingJobsTabel() {
        return pendingJobsTabel;
    }


    @Override
    public void onAddSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        addPendingJob(sportBuchungsJob);
    }

    @Override
    public void onRefreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        // TOTO refreshPendingJob
    }


    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }

    public List<SportBuchungsJob> getPendingBuchungsJobs() {
        return applicationStateDao.getPendingBuchungsJobs();
    }


}
