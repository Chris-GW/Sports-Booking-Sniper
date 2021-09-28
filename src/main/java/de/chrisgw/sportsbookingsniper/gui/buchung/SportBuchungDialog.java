package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.End;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class SportBuchungDialog extends DialogWindow {

    private final AtomicBoolean saved = new AtomicBoolean(false);
    private final ApplicationStateDao applicationStateDao;
    private final SportBuchungForm sportBuchungForm;


    public SportBuchungDialog(ApplicationStateDao applicationStateDao) {
        super("Sportbuchung");
        this.applicationStateDao = applicationStateDao;
        this.sportBuchungForm = new SportBuchungForm(applicationStateDao);
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED));
        setCloseWindowWithEscape(true);
        setComponent(createContentPane());
    }


    private Panel createContentPane() {
        Panel contentPane = new Panel();
        contentPane.addComponent(new Label("Bitte wählen Sie das zu buchende SportAngebot aus:"));
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(sportBuchungForm);
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel(), createLayoutData(End));
        return contentPane;
    }


    private Panel createLowerButtonPanel() {
        Panel lowerButtonPanel = new Panel();
        new Button(LocalizedString.Cancel.toString(), this::close).addTo(lowerButtonPanel);
        new Button("Zurücksetzen", this::resetSportBuchungsJob).addTo(lowerButtonPanel);
        new Button(LocalizedString.Save.toString(), this::saveSportBuchungsJob).addTo(lowerButtonPanel);
        lowerButtonPanel.setLayoutManager(new GridLayout(lowerButtonPanel.getChildCount()).setHorizontalSpacing(1));
        return lowerButtonPanel;
    }

    public void resetSportBuchungsJob() {
        setSportBuchungsJob(null);
    }

    public void saveSportBuchungsJob() {
        if (!sportBuchungForm.validateForm()) {
            saved.set(true);
            this.close();
        }
    }


    public SportBuchungsJob readSportBuchungsJob() {
        return sportBuchungForm.readFormValue();
    }

    public void setSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        sportBuchungForm.setFormValue(sportBuchungsJob);
    }


    @Override
    public Optional<SportBuchungsJob> showDialog(WindowBasedTextGUI textGUI) {
        saved.set(false);
        super.showDialog(textGUI);
        if (saved.get()) {
            return Optional.of(readSportBuchungsJob());
        } else {
            return Optional.empty();
        }
    }

}
