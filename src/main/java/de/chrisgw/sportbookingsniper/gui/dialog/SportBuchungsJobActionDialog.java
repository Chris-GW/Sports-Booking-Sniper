package de.chrisgw.sportbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import lombok.Getter;

import static de.chrisgw.sportbookingsniper.gui.dialog.SportBuchungsJobActionDialog.SportBuchungsJobActionDialogResult.*;


public class SportBuchungsJobActionDialog extends DialogWindow {

    @Getter
    private final SportBuchungsJob buchungsJob;
    @Getter
    private SportBuchungsJobActionDialogResult selectedActionResult = null;

    private Label descriptionLabel = new Label("");
    private ActionListBox actionListBox = new ActionListBox();


    public SportBuchungsJobActionDialog(SportBuchungsJob buchungsJob) {
        super("Sportbuchung");
        this.buchungsJob = buchungsJob;

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        setComponent(panel);

        panel.addComponent(setupDescriptionLabel());
        panel.addComponent(setupActionListBox());
    }


    private Label setupDescriptionLabel() {
        String sportArtName = buchungsJob.getSportArt().getName();
        String kursnummer = buchungsJob.getSportAngebot().getKursnummer();
        String formatTerminZeitraum = buchungsJob.getSportTermin().formatTerminZeitraum();
        descriptionLabel.setText(sportArtName + "\n(" + kursnummer + ") " + formatTerminZeitraum);
        return descriptionLabel;
    }

    private ActionListBox setupActionListBox() {
        actionListBox.addItem("Bearbeiten <???>", () -> {
            System.out.println("Bearbeiten: " + buchungsJob);
            selectedActionResult = BEARBEITEN;
        });
        actionListBox.addItem("Pausieren <Pause>", () -> {
            System.out.println("Pausieren: " + buchungsJob);
            selectedActionResult = PAUSIEREN;
        });
        actionListBox.addItem("Erneut Versuchen <???>", () -> {
            System.out.println("Erneut Versuchen: " + buchungsJob);
            selectedActionResult = ERNEUT_VERSUCHEN;
        });
        actionListBox.addItem("Buchung kopieren <S-c>", () -> {
            System.out.println("Buchung kopieren <S-c>" + buchungsJob);
            selectedActionResult = BUCHUNG_KOPIEREN;
        });
        actionListBox.addItem("Löschen <Entf>", () -> {
            System.out.println("Löschen: " + buchungsJob);
            selectedActionResult = LOESCHEN;
        });
        return actionListBox;
    }


    @Override
    public SportBuchungsJobActionDialogResult showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        return selectedActionResult;
    }


    public enum SportBuchungsJobActionDialogResult {
        BEARBEITEN, PAUSIEREN, ERNEUT_VERSUCHEN, BUCHUNG_KOPIEREN, LOESCHEN;
    }

}
