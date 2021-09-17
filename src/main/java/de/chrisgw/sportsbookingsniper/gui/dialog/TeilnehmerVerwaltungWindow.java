package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.AbstractListBox.ListItemRenderer;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.TeilnehmerListeListener;

import java.util.List;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class TeilnehmerVerwaltungWindow extends BasicWindow implements TeilnehmerListeListener {

    private final ApplicationStateDao applicationStateDao;

    private final ActionListBox teilnehmerListBox = new ActionListBox() {

        @Override
        public Result handleKeyStroke(KeyStroke keyStroke) {
            Result result = super.handleKeyStroke(keyStroke);
            runSelectedItem();
            return result;
        }
    };

    private final TeilnehmerFormPanel teilnehmerFormPanel = new TeilnehmerFormPanel();
    private final Button closeBtn = new Button(LocalizedString.Close.toString(), this::close);
    private final Button saveBtn = new Button(LocalizedString.Save.toString(), this::saveTeilnehmer);


    public TeilnehmerVerwaltungWindow(ApplicationStateDao applicationStateDao) {
        super("Teilnehmer Verwaltung");
        this.applicationStateDao = applicationStateDao;
        this.applicationStateDao.addTeilnehmerListeListener(this);

        teilnehmerListBox.setListItemRenderer(alwaysFocusedListItemRenderer());
        onChangedTeilnehmerListe(applicationStateDao.getTeilnehmerListe());

        Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.addComponent(teilnehmerListBox.withBorder(Borders.doubleLine()), Location.LEFT);
        mainPanel.addComponent(teilnehmerFormPanel.withBorder(Borders.singleLine()), Location.CENTER);
        mainPanel.addComponent(createLowerButtonPanel(), Location.BOTTOM);
        setComponent(mainPanel);
    }

    private Panel createLowerButtonPanel() {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(HORIZONTAL).setSpacing(1));
        buttonPanel.addComponent(closeBtn);
        buttonPanel.addComponent(new EmptySpace(), createLayoutData(Fill, CanGrow));
        buttonPanel.addComponent(saveBtn);
        return buttonPanel.setLayoutData(createLayoutData(Fill, CanGrow));
    }


    private ListItemRenderer<Runnable, ActionListBox> alwaysFocusedListItemRenderer() {
        return new ListItemRenderer<>() {

            @Override
            public void drawItem(TextGUIGraphics graphics, ActionListBox listBox, int index, Runnable item,
                    boolean selected, boolean focused) {
                super.drawItem(graphics, listBox, index, item, selected, true);
            }
        };
    }


    @Override
    public void onChangedTeilnehmerListe(List<Teilnehmer> changedTeilnehmerListe) {
        teilnehmerListBox.clearItems();
        teilnehmerListBox.addItem("neuer Teilnehmer", () -> this.selectTeilnehmer(new Teilnehmer()));
        for (Teilnehmer teilnehmer : applicationStateDao.getTeilnehmerListe()) {
            teilnehmerListBox.addItem(teilnehmer.getName(), () -> this.selectTeilnehmer(teilnehmer));
        }
        teilnehmerListBox.setSelectedIndex(0);
        teilnehmerListBox.runSelectedItem();
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
    }

    public void selectTeilnehmer(Teilnehmer teilnehmer) {
        teilnehmerFormPanel.setTeilnehmer(teilnehmer);
    }

    public Teilnehmer getSelectedTeilnehmer() {
        int selectedIndex = teilnehmerListBox.getSelectedIndex() - 1;
        return applicationStateDao.getTeilnehmerListe().get(selectedIndex);
    }

    public Teilnehmer readTeilnehmer() {
        return teilnehmerFormPanel.readTeilnehmer();
    }


    @Override
    public void close() {
        super.close();
        applicationStateDao.removeTeilnehmerListeListener(this);
    }

}
