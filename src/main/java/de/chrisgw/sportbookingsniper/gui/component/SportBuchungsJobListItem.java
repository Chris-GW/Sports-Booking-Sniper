package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.angebot.SportTermin;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;


public class SportBuchungsJobListItem extends AbstractInteractableComponent<SportBuchungsJobListItem> {

    private final Label label = new Label("");
    private final Label statusLabel = new Label("");
    private final CountdownProgressBar buchungCountdownBar = new CountdownProgressBar();
    private final CountdownProgressBar terminCountdownBar = new CountdownProgressBar();
    private final Panel panel = new Panel();

    @Getter
    @Setter
    private boolean selectedAndFocused = false;

    @Getter
    private SportBuchungsJob sportBuchungsJob;


    public SportBuchungsJobListItem(SportBuchungsJob sportBuchungsJob) {
        super();
        label.setLabelWidth(34);
        statusLabel.setLabelWidth(versuchStatusMaxLength());
        buchungCountdownBar.setPreferredWidth(4);
        terminCountdownBar.setPreferredWidth(4);

        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(label, LinearLayout.createLayoutData(Alignment.Beginning, GrowPolicy.None));
        panel.addComponent(statusLabel, LinearLayout.createLayoutData(Alignment.End, GrowPolicy.None));
        panel.addComponent(buchungCountdownBar, LinearLayout.createLayoutData(Alignment.Fill, GrowPolicy.CanGrow));
        panel.addComponent(terminCountdownBar, LinearLayout.createLayoutData(Alignment.Fill, GrowPolicy.CanGrow));
        setSportBuchungsJob(sportBuchungsJob);
    }

    private int versuchStatusMaxLength() {
        return Arrays.stream(SportBuchungsVersuchStatus.values())
                .map(SportBuchungsVersuchStatus::toString)
                .mapToInt(String::length)
                .max()
                .orElse(1);
    }


    public void setSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        this.sportBuchungsJob = sportBuchungsJob;
        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();
        SportBuchungsVersuch lastBuchungsVersuch = sportBuchungsJob.lastSportBuchungsVersuch();

        label.setText(formatLabelText(sportAngebot, sportTermin));
        setStatusLabel(lastBuchungsVersuch.getStatus());
        buchungCountdownBar.startCountdown(sportBuchungsJob.getBevorstehenderBuchungsVersuch());
        terminCountdownBar.startCountdown(sportTermin.getStartZeit());
        invalidate();
    }

    private String formatLabelText(SportAngebot sportAngebot, SportTermin sportTermin) {
        return " " + sportAngebot.getKursnummer() + " " + sportTermin.formatTerminZeitraum();
    }


    private void setStatusLabel(SportBuchungsVersuchStatus status) {
        statusLabel.setText(status.toString());
        statusLabel.setForegroundColor(toForegroundColor(status));
        statusLabel.setBackgroundColor(toBackgroundColor(status));
    }

    private TextColor toForegroundColor(SportBuchungsVersuchStatus status) {
        switch (status) {
        case BUCHUNG_GESCHLOSSEN:
            return ANSI.BLACK;
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
            return ANSI.CYAN_BRIGHT;
        case BUCHUNG_WARTELISTE:
            return ANSI.BLACK_BRIGHT;
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


    @Override
    protected InteractableRenderer<SportBuchungsJobListItem> createDefaultRenderer() {
        return new InteractableRenderer<SportBuchungsJobListItem>() {

            @Override
            public TerminalPosition getCursorLocation(SportBuchungsJobListItem component) {
                return new TerminalPosition(3, 0);
            }

            @Override
            public TerminalSize getPreferredSize(SportBuchungsJobListItem component) {
                return component.panel.getPreferredSize();
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, SportBuchungsJobListItem component) {
                ThemeDefinition themeDefinition = new ActionListBox().getTheme().getDefinition(AbstractListBox.class);
                if (selectedAndFocused) {
                    TextColor foregroundColor = themeDefinition.getSelected().getForeground();
//                    if (ANSI.WHITE.equals(foregroundColor)) {
//                        foregroundColor = ANSI.WHITE_BRIGHT;
//                    }
                    label.setForegroundColor(foregroundColor);
                    label.setBackgroundColor(themeDefinition.getSelected().getBackground());
                } else {
                    label.setForegroundColor(null);
                    label.setBackgroundColor(null);
                }
                component.panel.draw(graphics);
            }

        };
    }

}
