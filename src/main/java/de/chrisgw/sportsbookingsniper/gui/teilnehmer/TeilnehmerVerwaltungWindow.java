package de.chrisgw.sportsbookingsniper.gui.teilnehmer;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.util.List;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class TeilnehmerVerwaltungWindow extends BasicWindow {

    private final ApplicationStateDao applicationStateDao;

    private final TeilnehmerListBox teilnehmerListBox;
    private final Label centerHeaderLabel = new Label("Teilnehmer Angaben");
    private final TeilnehmerFormPanel teilnehmerFormPanel = new TeilnehmerFormPanel();

    private final Button closeBtn = new Button(LocalizedString.Close.toString(), this::close);
    private final Button saveBtn = new Button(LocalizedString.Save.toString(), this::saveTeilnehmer);
    private final Button deleteBtn = new Button("Löschen", this::deleteTeilnehmer);


    public TeilnehmerVerwaltungWindow(ApplicationStateDao applicationStateDao) {
        super("Teilnehmer Verwaltung");
        this.applicationStateDao = applicationStateDao;
        teilnehmerListBox = new TeilnehmerListBox(applicationStateDao);
        teilnehmerListBox.addSelectedItemListener((this::setTeilnehmerForm));
        teilnehmerListBox.addSelectedItemListener((index, teilnehmer) -> setEnabledDeleteBtn());
        setHints(List.of(Hint.CENTERED));
        setCloseWindowWithEscape(true);

        Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.addComponent(teilnehmerListBox.withBorder(Borders.doubleLine()), Location.LEFT);
        mainPanel.addComponent(createCenterPanel().withBorder(Borders.singleLine()), Location.CENTER);
        mainPanel.addComponent(createLowerButtonPanel(), Location.BOTTOM);
        setComponent(mainPanel);
    }

    private void setTeilnehmerForm(int index, Teilnehmer teilnehmer) {
        if (teilnehmer != null && index > 0) {
            centerHeaderLabel.setText("Angaben zum Teilnehmer/in '" + teilnehmer.getName() + "' bearbeiten");
        } else {
            centerHeaderLabel.setText("Angaben zum neuen Teilnehmer/in eingeben");
        }
        teilnehmerFormPanel.setTeilnehmer(teilnehmer);
    }

    private Panel createCenterPanel() {
        Panel centerPanel = new Panel();
        centerPanel.addComponent(centerHeaderLabel);
        centerPanel.addComponent(new EmptySpace());
        centerPanel.addComponent(teilnehmerFormPanel);
        return centerPanel;
    }


    private Panel createLowerButtonPanel() {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(HORIZONTAL).setSpacing(1));
        buttonPanel.addComponent(closeBtn);
        buttonPanel.addComponent(new EmptySpace(), createLayoutData(Fill, CanGrow));
        buttonPanel.addComponent(deleteBtn);
        buttonPanel.addComponent(saveBtn);
        setEnabledDeleteBtn();
        return buttonPanel.setLayoutData(createLayoutData(Fill, CanGrow));
    }


    private void saveTeilnehmer() {
        int selectedIndex = teilnehmerListBox.getSelectedIndex();
        Teilnehmer readTeilnehmer = teilnehmerFormPanel.readTeilnehmer();
        if (selectedIndex == 0) {
            applicationStateDao.addTeilnehmer(readTeilnehmer);
        } else if (selectedIndex > 0) {
            applicationStateDao.updateTeilnehmer(selectedIndex - 1, readTeilnehmer);
        }
        new MessageDialogBuilder() //
                .setTitle("Teilnehmer erfolgreich gespeichert") //
                .setText(String.format("Der Teilnehmer mit dem Namen '%s' wurde erfolgreich gespeichert!", //
                        readTeilnehmer.getName())) //
                .build() //
                .showDialog(getTextGUI());
        teilnehmerListBox.takeFocus();
    }

    private void deleteTeilnehmer() {
        Teilnehmer teilnehmer = teilnehmerListBox.getSelectedItem();
        applicationStateDao.removeTeilnehmer(teilnehmer);
        new MessageDialogBuilder() //
                .setTitle("Teilnehmer erfolgreich gelöscht") //
                .setText(String.format("Der Teilnehmer mit dem Namen '%s' wurde erfolgreich gelöscht!", //
                        teilnehmer.getName())) //
                .build() //
                .showDialog(getTextGUI());
        teilnehmerListBox.takeFocus();
    }

    private void setEnabledDeleteBtn() {
        deleteBtn.setEnabled(teilnehmerListBox.getSelectedIndex() > 0);
    }

}
