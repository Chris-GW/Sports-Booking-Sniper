package de.chrisgw.sportbooking.gui.window;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.*;
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

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class PendingSportBuchungenWindow extends BasicWindow implements SportBuchungJobListener {

    private final ApplicationStateDao applicationStateDao;
    private final Table<String> pendingJobsTabel;


    public PendingSportBuchungenWindow(ApplicationStateDao applicationStateDao) {
        super("Ausstehende Sportbuchungen");
        this.applicationStateDao = applicationStateDao;
        Panel contentPanel = new Panel(new GridLayout(1).setTopMarginSize(1).setBottomMarginSize(1));

        pendingJobsTabel = new Table<>("#", "Sportangebot", "Details");
        pendingJobsTabel.setVisibleRows(6);
        pendingJobsTabel.setVisibleColumns(3);
        pendingJobsTabel.setSelectAction(this::onSelectPendingJob);
        getPendingBuchungsJobs().forEach(this::addPendingJob);
        contentPanel.addComponent(pendingJobsTabel, createLayoutData(Fill));
        setComponent(contentPanel);
    }





    @Override
    public void draw(TextGUIGraphics graphics) {
        super.draw(graphics);
        graphics.putString(TerminalPosition.TOP_LEFT_CORNER,
                String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));
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
                .showDialog((WindowBasedTextGUI) getTextGUI());
    }


    @Override
    public void onAddSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        addPendingJob(sportBuchungsJob);
    }

    @Override
    public void onRefreshSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        refreshPendingBuchungsJob(sportBuchungsJob);
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            applicationStateDao.addSportBuchungJobListener(this);
        } else {
            applicationStateDao.removeSportBuchungJobListener(this);
        }
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


    private void refreshPendingBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        // TOTO refreshPendingJob
    }


    public List<SportBuchungsJob> getPendingBuchungsJobs() {
        return applicationStateDao.getPendingBuchungsJobs();
    }


}
