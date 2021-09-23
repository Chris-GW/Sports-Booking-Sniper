package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.component.CountdownProgressBar;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;


public class PendingSportBuchungsJobComponent extends AbstractInteractableComponent<PendingSportBuchungsJobComponent>
        implements SportBuchungsJobListener {

    private final ApplicationStateDao applicationStateDao;
    private final SportBuchungsJob sportBuchungsJob;

    private final Panel rowPanel = new Panel();
    private final Label kursnummerLabel = new Label("");
    private final Label sportTerimLabel = new Label("");
    private final Label statusLabel = new Label("");
    private final CountdownProgressBar buchungCountdownProgressBar = new CountdownProgressBar();
    private final CountdownProgressBar terminCountdownProgressBar = new CountdownProgressBar();


    public PendingSportBuchungsJobComponent(ApplicationStateDao applicationStateDao,
            SportBuchungsJob sportBuchungsJob) {
        this.applicationStateDao = applicationStateDao;
        this.sportBuchungsJob = sportBuchungsJob;

        rowPanel.setLayoutManager(new GridLayout(5).setHorizontalSpacing(1));
        rowPanel.addComponent(kursnummerLabel);
        rowPanel.addComponent(sportTerimLabel);
        rowPanel.addComponent(statusLabel);
        rowPanel.addComponent(buchungCountdownProgressBar);
        rowPanel.addComponent(terminCountdownProgressBar);
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        rowPanel.onAdded(container);

    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        rowPanel.onRemoved(container);
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        // no-op
    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        if (isSameSportBuchungsJob(sportBuchungsJob)) {
            invalidate();
        }
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        if (isSameSportBuchungsJob(sportBuchungsJob)) {
            setVisible(false);
            getParent().removeComponent(this);
        }
    }

    private boolean isSameSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        return this.sportBuchungsJob.getJobId() == sportBuchungsJob.getJobId();
    }


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();
        kursnummerLabel.setText(sportAngebot.getKursnummer());
        sportTerimLabel.setText(sportTermin.toString());
        statusLabel.setText(sportBuchungsJob.getLastBuchungsVersuchStatus().toString());
        buchungCountdownProgressBar.startCountdown(sportBuchungsJob.getBevorstehenderBuchungsVersuch());
        terminCountdownProgressBar.startCountdown(sportTermin.getStartZeit());
    }


    @Override
    protected Result handleKeyStroke(KeyStroke keyStroke) {
        if (isKeyboardActivationStroke(keyStroke)) {
            showBuchungsJobActionListDialog();
            return Result.HANDLED;
        } else {
            return super.handleKeyStroke(keyStroke);
        }
    }

    private void showBuchungsJobActionListDialog() {
        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();
        new ActionListDialogBuilder() //
                .setTitle("Ausgewählter Sport Buchungs Job ...")
                .setDescription(sportAngebot.getName() + "\n" + sportTermin.toString())
                .setCanCancel(true)
                .setCloseAutomaticallyOnAction(true)
                .addAction("Bearbeiten <c-b>", () -> {})
                .addAction("Jetzt Versuchen zu buchen <F5>", () -> {})
                .addAction("Buchung kopieren <S-c>", () -> {})
                .addAction("Löschen <Entf>", () -> {})
                .build()
                .showDialog(getTextGUI());
    }

    @Override
    protected InteractableRenderer<PendingSportBuchungsJobComponent> createDefaultRenderer() {
        return new InteractableRenderer<>() {

            @Override
            public TerminalSize getPreferredSize(PendingSportBuchungsJobComponent component) {
                return rowPanel.getPreferredSize();
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, PendingSportBuchungsJobComponent component) {
                rowPanel.draw(graphics);
            }

            @Override
            public TerminalPosition getCursorLocation(PendingSportBuchungsJobComponent component) {
                return TerminalPosition.TOP_LEFT_CORNER;
            }

        };
    }

    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid() || rowPanel.isInvalid();
    }
}
