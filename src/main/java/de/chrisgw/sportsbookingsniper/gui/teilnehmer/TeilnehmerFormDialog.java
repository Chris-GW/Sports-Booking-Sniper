package de.chrisgw.sportsbookingsniper.gui.teilnehmer;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.component.ShortKeyRegistry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class TeilnehmerFormDialog extends DialogWindow {

    private final ShortKeyRegistry shortKeyRegistry = new ShortKeyRegistry();
    private final AtomicBoolean saved = new AtomicBoolean(false);
    private boolean forceValidTeilnehmerForm;

    private final TeilnehmerForm teilnehmerFormularPanel = new TeilnehmerForm();
    private final Button cancelBtn = new Button(LocalizedString.Cancel.toString(), this::close);
    private final Button resetBtn = new Button("Zurücksetzen", this::resetTeilnehmerForm);
    private final Button saveBtn = new Button(LocalizedString.Save.toString(), this::saveTeilnehmerForm);


    public TeilnehmerFormDialog() {
        this(null);
    }

    public TeilnehmerFormDialog(Teilnehmer teilnehmer) {
        super("Angaben zum Teilnehmer");
        setHints(List.of(Hint.MODAL, Hint.CENTERED));
        setForceValidTeilnehmerForm(false);
        addBasePaneListener(shortKeyRegistry);

        Panel contentPane = new Panel();
        contentPane.addComponent(teilnehmerFormularPanel);
        contentPane.addComponent(new EmptySpace());
        contentPane.addComponent(createLowerButtonPanel());
        setComponent(contentPane);
        setTeilnehmer(teilnehmer);
    }


    private Panel createLowerButtonPanel() {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(HORIZONTAL).setSpacing(1));
        buttonPanel.addComponent(cancelBtn);
        buttonPanel.addComponent(new EmptySpace(), createLayoutData(Fill, CanGrow));
        buttonPanel.addComponent(resetBtn);
        buttonPanel.addComponent(saveBtn);
        shortKeyRegistry.registerButtonAction(new KeyStroke('z', true, false), resetBtn);
        shortKeyRegistry.registerButtonAction(new KeyStroke('s', true, false), saveBtn);
        return buttonPanel.setLayoutData(createLayoutData(Fill, CanGrow));
    }

    private void saveTeilnehmerForm() {
        if (!teilnehmerFormularPanel.validateForm()) {
            saved.set(true);
            this.close();
        }
    }


    public Teilnehmer readTeilnehmer() {
        return teilnehmerFormularPanel.readFormValue();
    }

    public void setTeilnehmer(Teilnehmer teilnehmer) {
        teilnehmerFormularPanel.setFormValue(teilnehmer);
    }

    public void resetTeilnehmerForm() {
        teilnehmerFormularPanel.resetFormValue();
    }


    public boolean isForceValidTeilnehmerForm() {
        return forceValidTeilnehmerForm;
    }

    public void setForceValidTeilnehmerForm(boolean forceValidTeilnehmerForm) {
        this.forceValidTeilnehmerForm = forceValidTeilnehmerForm;
        setCloseWindowWithEscape(!forceValidTeilnehmerForm);
        cancelBtn.setEnabled(!forceValidTeilnehmerForm);
    }


    @Override
    public Optional<Teilnehmer> showDialog(WindowBasedTextGUI textGUI) {
        saved.set(false);
        super.showDialog(textGUI);
        if (saved.get()) {
            return Optional.of(readTeilnehmer());
        } else {
            return Optional.empty();
        }
    }

}
