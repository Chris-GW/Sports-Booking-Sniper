package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.ApplicationStateDao.FinishedSportBuchungenListener;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class FinishedSportBuchungenComponent extends SportBookingComponent implements FinishedSportBuchungenListener {

    @Getter
    private final Table<String> finishedJobsTabel;


    public FinishedSportBuchungenComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "Beendete SportBuchungen", KeyType.F3);
        setLayoutManager(new BorderLayout());

        this.finishedJobsTabel = createFinishedJobsTable();
        getFinishedBuchungsJobs().forEach(this::addFinishedBuchungsJob);

        addComponent(finishedJobsTabel, Location.CENTER);
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addFinishedSportBuchungenListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeFinishedSportBuchungenListener(this);
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
    public void onAddFinishedSportBuchung(SportBuchungsBestaetigung sportBuchungsBestaetigung) {
        addFinishedBuchungsJob(sportBuchungsBestaetigung);
    }


    private List<SportBuchungsBestaetigung> getFinishedBuchungsJobs() {
        return applicationStateDao.getFinishedBuchungsJobs();
    }


    @Override
    protected FinishedSportBuchungenComponent self() {
        return this;
    }

}
