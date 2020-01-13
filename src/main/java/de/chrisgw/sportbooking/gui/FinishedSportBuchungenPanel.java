package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportTermin;
import de.chrisgw.sportbooking.service.SavedApplicationDataService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class FinishedSportBuchungenPanel extends Panel {

    private final SavedApplicationDataService savedApplicationDataService;
    private final Table<String> finishedJobsTabel;


    public FinishedSportBuchungenPanel(SavedApplicationDataService savedApplicationDataService) {
        super(new LinearLayout(Direction.VERTICAL));
        this.savedApplicationDataService = savedApplicationDataService;

        finishedJobsTabel = new Table<>("#", "Sportart", "Kursnummer", "Details", "Ort", "Startzeit", "Endzeit");
        finishedJobsTabel.setSelectAction(this::onSelectFinishedJob);
        addComponent(finishedJobsTabel);
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
                .showDialog((WindowBasedTextGUI) getTextGUI());
    }


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        for (int i = 0; i < finishedJobsTabel.getTableModel().getRowCount(); i++) {
            finishedJobsTabel.getTableModel().removeRow(0);
        }
        getFinishedBuchungsJobs().forEach(this::addFinishedBuchungsJob);
    }


    private void addFinishedBuchungsJob(SportBuchungsBestaetigung finishedBuchungsJob) {
        List<String> rowValues = new ArrayList<>();
        SportTermin sportTermin = finishedBuchungsJob.getSportTermin();
        SportAngebot sportAngebot = sportTermin.getSportAngebot();

        rowValues.add(String.valueOf(finishedBuchungsJob.getJobId()));
        rowValues.add(sportAngebot.getSportArt().getName());
        rowValues.add(sportAngebot.getKursnummer());
        rowValues.add(sportAngebot.getDetails());
        rowValues.add(sportAngebot.getOrt());
        rowValues.add(formatSportTerminStartZeit(sportTermin));
        rowValues.add(formatSportTerminEndZeit(sportTermin));
        finishedJobsTabel.getTableModel().addRow(rowValues);
    }

    private String formatSportTerminStartZeit(SportTermin sportTermin) {
        return DateTimeFormatter.ofPattern("c. dd.MM HH:mm").format(sportTermin.getStartZeit());
    }

    private String formatSportTerminEndZeit(SportTermin sportTermin) {
        return DateTimeFormatter.ofPattern("c. dd.MM.YYYY HH:mm").format(sportTermin.getEndZeit());
    }


    public List<SportBuchungsBestaetigung> getFinishedBuchungsJobs() {
        return savedApplicationDataService.getSavedApplicationData().getFinishedBuchungsJobs();
    }

}
