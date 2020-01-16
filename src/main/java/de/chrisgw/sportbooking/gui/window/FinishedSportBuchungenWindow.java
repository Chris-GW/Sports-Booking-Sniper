package de.chrisgw.sportbooking.gui.window;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.ApplicationStateDao.FinishedSportBuchungenListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;


public class FinishedSportBuchungenWindow extends BasicWindow implements FinishedSportBuchungenListener {

    private final ApplicationStateDao applicationStateDao;
    private final Table<String> finishedJobsTabel;


    public FinishedSportBuchungenWindow(ApplicationStateDao applicationStateDao) {
        super("Beendete Sportbuchungen");
        this.applicationStateDao = applicationStateDao;
        this.applicationStateDao.addFinishedSportBuchungenListener(this);

        this.finishedJobsTabel = createFinishedJobsTable();
        getFinishedBuchungsJobs().forEach(this::addFinishedBuchungsJob);

        Panel contentPanel = new Panel(new GridLayout(1).setTopMarginSize(1).setBottomMarginSize(1));
        contentPanel.addComponent(finishedJobsTabel, GridLayout.createLayoutData(FILL, BEGINNING, true, false));
        setComponent(contentPanel);
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
        if (selectedRow < 0) {
            return;
        }
        SportBuchungsBestaetigung selectedBestaetigung = getFinishedBuchungsJobs().get(selectedRow);

        new ActionListDialogBuilder().setTitle("Beendete Sportbuchung")
                .setDescription("Aktion bitte auswählen")
                .setCanCancel(true)
                .addAction("Details", () -> {
                    System.out.println("Details: " + selectedBestaetigung);
                })
                .addAction("Löschen", () -> {
                    System.out.println("Löschen: " + selectedBestaetigung);
                })
                .build()
                .showDialog(getTextGUI());
    }


    private void addFinishedBuchungsJob(SportBuchungsBestaetigung finishedBuchungsJob) {
        List<String> rowValues = new ArrayList<>();
        SportTermin sportTermin = finishedBuchungsJob.getSportTermin();
        SportAngebot sportAngebot = sportTermin.getSportAngebot();
        String kursnummer = sportAngebot.getKursnummer();
        String sportArtName = sportAngebot.getSportArt().getName();
        LocalDateTime timestamp = finishedBuchungsJob.getTimestamp();
        String formatBuchungsTimestamp = DateTimeFormatter.ofPattern("dd.MM. HH:mm").format(timestamp);

        rowValues.add(String.valueOf(finishedBuchungsJob.getJobId()));
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
    public void draw(TextGUIGraphics graphics) {
        super.draw(graphics);
        graphics.putString(TerminalPosition.TOP_LEFT_CORNER,
                String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));
    }


    @Override
    public void onAddFinishedSportBuchung(SportBuchungsBestaetigung sportBuchungsBestaetigung) {
        addFinishedBuchungsJob(sportBuchungsBestaetigung);
    }


    private List<SportBuchungsBestaetigung> getFinishedBuchungsJobs() {
        return applicationStateDao.getFinishedBuchungsJobs();
    }

}
