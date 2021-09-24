package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button.FlatButtonRenderer;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.gui.component.CountdownProgressBar;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.versuchStatusMaxLength;


public class AusstehendeSportBuchungsJobItem extends Panel implements SportBuchungsJobListener {

    @Getter
    private final SportBuchungsJob sportBuchungsJob;

    private final Button buchungsJobBtn;
    private final Label statusLabel = new Label("");
    private final CountdownProgressBar buchungCountdownProgressBar = new CountdownProgressBar();


    public AusstehendeSportBuchungsJobItem(SportBuchungsJob sportBuchungsJob) {
        this.sportBuchungsJob = sportBuchungsJob;

        buchungsJobBtn = createBuchungsJobBtn();
        statusLabel.setLabelWidth(versuchStatusMaxLength());
        buchungCountdownProgressBar.startCountdown(sportBuchungsJob.getBevorstehenderBuchungsVersuch());

        setLayoutManager(new LinearLayout(HORIZONTAL));
        addComponent(buchungsJobBtn);
        addComponent(statusLabel);
        addComponent(buchungCountdownProgressBar, createLayoutData(Fill, CanGrow));
    }

    private Button createBuchungsJobBtn() {
        Button buchungsJobBtn = new Button("", this::showBuchungsJobActionListDialog);
        buchungsJobBtn.setRenderer(flatListItemBtnRenderer());
        return buchungsJobBtn;
    }

    private FlatButtonRenderer flatListItemBtnRenderer() {
        return new FlatButtonRenderer() {

            @Override
            public void drawComponent(TextGUIGraphics graphics, Button button) {
                ThemeDefinition themeDefinition = button.getTheme().getDefaultDefinition();
                if (button.isFocused()) {
                    graphics.applyThemeStyle(themeDefinition.getActive());
                } else {
                    graphics.applyThemeStyle(themeDefinition.getInsensitive());
                }
                graphics.fill(' ');
                if (button.isFocused()) {
                    graphics.applyThemeStyle(themeDefinition.getSelected());
                } else {
                    graphics.applyThemeStyle(themeDefinition.getNormal());
                }
                graphics.putString(0, 0, button.getLabel());
            }
        };
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
            if (getParent() != null) {
                getParent().removeComponent(this);
            }
        }
    }

    private boolean isSameSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        return this.sportBuchungsJob.getJobId() == sportBuchungsJob.getJobId();
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
    public boolean isInvalid() {
        return super.isInvalid() || buchungCountdownProgressBar.isInvalid();
    }


    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        String kursnummer = sportBuchungsJob.getSportAngebot().getKursnummer();
        String formatTerminZeitraum = sportBuchungsJob.getSportTermin().formatTerminZeitraum();

        buchungsJobBtn.setLabel(kursnummer + " " + formatTerminZeitraum);
        onBeforeDrawingStatusLabel();
        if (buchungCountdownProgressBar.remainingDuration().isZero()) {
            buchungCountdownProgressBar.startCountdown(sportBuchungsJob.getBevorstehenderBuchungsVersuch());
        }
    }


    private void onBeforeDrawingStatusLabel() {
        SportBuchungsVersuchStatus lastBuchungsVersuchStatus = sportBuchungsJob.getLastBuchungsVersuchStatus();
        String statusText = StringUtils.rightPad(lastBuchungsVersuchStatus.toString(), versuchStatusMaxLength());
        statusLabel.setText(statusText);
        statusLabel.setForegroundColor(toForegroundColor(lastBuchungsVersuchStatus));
        statusLabel.setBackgroundColor(toBackgroundColor(lastBuchungsVersuchStatus));
    }

    private TextColor toForegroundColor(SportBuchungsVersuchStatus status) {
        switch (status) {
        case BUCHUNG_GESCHLOSSEN:
            return ANSI.WHITE_BRIGHT;
        case BUCHUNG_WARTELISTE:
            return ANSI.BLACK;
        case BUCHUNG_ABGELAUFEN:
            return ANSI.BLACK;
        case BUCHUNG_ERFOLGREICH:
            return ANSI.BLACK;
        case BUCHUNG_FEHLER:
            return ANSI.BLACK;
        default:
            throw new IllegalArgumentException("unknown status " + status);
        }
    }

    private TextColor toBackgroundColor(SportBuchungsVersuchStatus status) {
        switch (status) {
        case BUCHUNG_GESCHLOSSEN:
            return ANSI.BLACK_BRIGHT;
        case BUCHUNG_WARTELISTE:
            return ANSI.CYAN_BRIGHT;
        case BUCHUNG_ABGELAUFEN:
            return ANSI.YELLOW_BRIGHT;
        case BUCHUNG_ERFOLGREICH:
            return ANSI.GREEN_BRIGHT;
        case BUCHUNG_FEHLER:
            return ANSI.RED_BRIGHT;
        default:
            throw new IllegalArgumentException("unknown status " + status);
        }
    }

}
