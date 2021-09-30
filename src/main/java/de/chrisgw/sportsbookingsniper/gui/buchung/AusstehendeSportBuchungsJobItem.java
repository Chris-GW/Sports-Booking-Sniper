package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button.FlatButtonRenderer;
import com.googlecode.lanterna.gui2.Button.Listener;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.gui.component.CountdownProgressBar;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.versuchStatusMaxLength;


public class AusstehendeSportBuchungsJobItem extends Panel {

    @Getter
    private final SportBuchungsJob sportBuchungsJob;

    private final Button buchungsJobBtn = new Button("").setRenderer(flatListItemBtnRenderer());
    private final Label statusLabel = new Label("").setLabelWidth(versuchStatusMaxLength());
    private final CountdownProgressBar buchungCountdownProgressBar = new CountdownProgressBar();


    public AusstehendeSportBuchungsJobItem(SportBuchungsJob sportBuchungsJob) {
        this.sportBuchungsJob = sportBuchungsJob;
        setLayoutManager(new LinearLayout(HORIZONTAL));
        addComponent(buchungsJobBtn);
        addComponent(statusLabel);
        addComponent(buchungCountdownProgressBar, createLayoutData(Fill, CanGrow));
    }


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        updateBuchungsJobBtnLabel();
        updateStatusLabel();
        startCountdownProgressBarIfNeeded();
    }

    private void updateBuchungsJobBtnLabel() {
        String kursnummer = sportBuchungsJob.getSportAngebot().getKursnummer();
        String formatTerminZeitraum = sportBuchungsJob.getSportTermin().formatTerminZeitraum();
        buchungsJobBtn.setLabel(kursnummer + " " + formatTerminZeitraum);
    }


    private void startCountdownProgressBarIfNeeded() {
        LocalDateTime bevorstehenderBuchungsVersuch = sportBuchungsJob.getBevorstehenderBuchungsVersuch();
        LocalDateTime countdownEndTime = buchungCountdownProgressBar.getCountdownEndTime();
        if (bevorstehenderBuchungsVersuch.isAfter(countdownEndTime)) {
            buchungCountdownProgressBar.startCountdown(bevorstehenderBuchungsVersuch);
        }
    }


    private void updateStatusLabel() {
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


    public void addListener(Listener listener) {
        buchungsJobBtn.addListener(listener);
    }

    public boolean removeListener(Listener listener) {
        return buchungsJobBtn.removeListener(listener);
    }


    @Override
    public boolean isInvalid() {
        return super.isInvalid() || buchungCountdownProgressBar.isInvalid();
    }

}
