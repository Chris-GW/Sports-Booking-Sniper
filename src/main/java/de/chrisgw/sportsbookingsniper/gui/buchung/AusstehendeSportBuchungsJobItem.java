package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.gui.component.CountdownProgressBar;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.versuchStatusMaxLength;


public class AusstehendeSportBuchungsJobItem extends AbstractInteractableComponent<AusstehendeSportBuchungsJobItem>
        implements SportBuchungsJobListener {

    @Getter
    private final SportBuchungsJob sportBuchungsJob;
    private final CountdownProgressBar buchungCountdownProgressBar = new CountdownProgressBar();


    public AusstehendeSportBuchungsJobItem(SportBuchungsJob sportBuchungsJob) {
        this.sportBuchungsJob = sportBuchungsJob;
        buchungCountdownProgressBar.startCountdown(sportBuchungsJob.getBevorstehenderBuchungsVersuch());
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
    public boolean isInvalid() {
        return super.isInvalid() || buchungCountdownProgressBar.isInvalid();
    }


    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }


    @Override
    protected InteractableRenderer<AusstehendeSportBuchungsJobItem> createDefaultRenderer() {
        return new PendingSportBuchungsJobComponentRenderer();
    }


    public static class PendingSportBuchungsJobComponentRenderer
            implements InteractableRenderer<AusstehendeSportBuchungsJobItem> {

        private final TerminalSize sportAngebotSize = new TerminalSize("12345678 Do 24.07. 18:00-19:30".length(), 1);
        private final TerminalSize statusLabelSize = new TerminalSize(versuchStatusMaxLength(), 1);


        @Override
        public TerminalSize getPreferredSize(AusstehendeSportBuchungsJobItem component) {
            return sportAngebotSize.withRelative(statusLabelSize).withRows(1).withRelativeColumns(9);
        }

        @Override
        public void drawComponent(TextGUIGraphics graphics, AusstehendeSportBuchungsJobItem component) {
            if (component.isFocused()) {
                graphics.newTextGraphics(TOP_LEFT_CORNER, sportAngebotSize.withRelativeColumns(1).withRows(1))
                        .applyThemeStyle(component.getThemeDefinition().getActive())
                        .fill(' ');
            }

            TerminalPosition position = TOP_LEFT_CORNER;
            drawSportAngebotInfos(graphics.newTextGraphics(position, sportAngebotSize), component);
            position = position.withRelativeColumn(sportAngebotSize.getColumns() + 1);

            drawBuchungsStatusLabel(graphics.newTextGraphics(position, statusLabelSize), component);
            position = position.withRelativeColumn(statusLabelSize.getColumns() + 1);

            int consumedSize = sportAngebotSize.getColumns() + statusLabelSize.getColumns() + 2;
            TerminalSize buchungsCountdownSize = graphics.getSize().withRelativeColumns(-consumedSize);
            drawBuchungsCountdown(graphics.newTextGraphics(position, buchungsCountdownSize), component);
        }

        private void drawSportAngebotInfos(TextGUIGraphics graphics, AusstehendeSportBuchungsJobItem component) {
            if (component.isFocused()) {
                graphics.applyThemeStyle(component.getThemeDefinition().getActive());
            } else {
                graphics.applyThemeStyle(component.getThemeDefinition().getNormal());
            }
            SportAngebot sportAngebot = component.sportBuchungsJob.getSportAngebot();
            SportTermin sportTermin = component.sportBuchungsJob.getSportTermin();
            graphics.putString(TOP_LEFT_CORNER, sportAngebot.getKursnummer());
            graphics.putString(TOP_LEFT_CORNER.withRelativeColumn(9), sportTermin.formatTerminZeitraum());
        }


        private void drawBuchungsStatusLabel(TextGUIGraphics graphics, AusstehendeSportBuchungsJobItem component) {
            var status = component.sportBuchungsJob.getLastBuchungsVersuchStatus();
            String statusText = StringUtils.center(status.toString(), versuchStatusMaxLength());
            graphics.applyThemeStyle(component.getTheme().getDefaultDefinition().getNormal())
                    .setForegroundColor(toForegroundColor(status))
                    .setBackgroundColor(toBackgroundColor(status))
                    .putString(TOP_LEFT_CORNER, statusText);
        }


        private TextColor toForegroundColor(SportBuchungsVersuchStatus status) {
            switch (status) {
            case BUCHUNG_GESCHLOSSEN:
                return ANSI.BLACK;
            case BUCHUNG_WARTELISTE:
                return ANSI.WHITE_BRIGHT;
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


        private void drawBuchungsCountdown(TextGUIGraphics newTextGraphics, AusstehendeSportBuchungsJobItem component) {
            component.buchungCountdownProgressBar.draw(newTextGraphics);
        }


        @Override
        public TerminalPosition getCursorLocation(AusstehendeSportBuchungsJobItem component) {
            return TOP_LEFT_CORNER;
        }

    }

}
