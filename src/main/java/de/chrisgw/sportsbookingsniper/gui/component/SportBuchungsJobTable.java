package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.DefaultTableCellRenderer;
import com.googlecode.lanterna.gui2.table.DefaultTableRenderer;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.gui.component.SportBuchungsJobTable.SportBuchungsJobCell;
import de.chrisgw.sportsbookingsniper.gui.dialog.SportBuchungsJobActionDialog;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;
import static com.googlecode.lanterna.TerminalTextUtils.getColumnWidth;
import static de.chrisgw.sportsbookingsniper.gui.component.SportBuchungsJobTable.SportBuchungsJobCellType.*;


public class SportBuchungsJobTable extends Table<SportBuchungsJobCell> {

    private NavigableMap<SportArt, NavigableSet<SportBuchungsJob>> sportArtWithJobs = new TreeMap<>();


    public SportBuchungsJobTable() {
        super(null, null, null, null);
        setRenderer(newTableRenderer());
        setTableCellRenderer(new SportBuchungsJobTableCellRenderer());
        setSelectAction(showSportBuchungsJobContextMenu());
    }


    private Runnable showSportBuchungsJobContextMenu() {
        return () -> {
            SportBuchungsJob buchungsJob = getSelectedSportBuchungsJob();
            WindowBasedTextGUI windowBasedTextGUI = (WindowBasedTextGUI) getTextGUI();
            new SportBuchungsJobActionDialog(buchungsJob).showDialog(windowBasedTextGUI);
        };
    }


    private DefaultTableRenderer<SportBuchungsJobCell> newTableRenderer() {
        DefaultTableRenderer<SportBuchungsJobCell> defaultTableRenderer = new DefaultTableRenderer<>();
        defaultTableRenderer.setExpandableColumns(Arrays.asList(2, 3));
        defaultTableRenderer.setAllowPartialColumn(true);
        return defaultTableRenderer;
    }


    public void addSportBuchungsJob(SportBuchungsJob newBuchungsJob) {
        SportArt sportArt = newBuchungsJob.getSportArt();
        getSportBuchungsJobs(sportArt).add(newBuchungsJob);
        rebuildTableCells();
    }

    private NavigableSet<SportBuchungsJob> getSportBuchungsJobs(SportArt sportArt) {
        return sportArtWithJobs.computeIfAbsent(sportArt, sportArt1 -> {
            return new TreeSet<>(Comparator.comparing(SportBuchungsJob::getSportTermin));
        });
    }

    private void rebuildTableCells() {
        TableModel<SportBuchungsJobCell> tableModel = getTableModel();
        tableModel.clear();
        for (SportArt sportArt : sportArtWithJobs.keySet()) {
            NavigableSet<SportBuchungsJob> buchungsJobs = sportArtWithJobs.get(sportArt);
            tableModel.addRow(newSportArtHeaderRow(buchungsJobs));
            for (SportBuchungsJob sportBuchungsJob : buchungsJobs) {
                tableModel.addRow(newSportBuchungsJobRow(sportBuchungsJob));
            }
        }
        int neededRows = sportArtWithJobs.size() + sportArtWithJobs.values().stream().mapToInt(Collection::size).sum();
        setPreferredSize(new TerminalSize(80, neededRows));
        invalidate();
    }

    private List<SportBuchungsJobCell> newSportArtHeaderRow(NavigableSet<SportBuchungsJob> buchungsJobs) {
        SportBuchungsJobCell sportArtHeaderCell = new SportBuchungsJobCell(SPORT_ART_HEADER_CELL, buchungsJobs.first());
        return Stream.generate(() -> sportArtHeaderCell)
                .limit(getTableModel().getColumnCount())
                .collect(Collectors.toList());
    }

    private List<SportBuchungsJobCell> newSportBuchungsJobRow(SportBuchungsJob sportBuchungsJob) {
        List<SportBuchungsJobCell> rowCells = new ArrayList<>(4);
        rowCells.add(new SportBuchungsJobCell(JOB_LABEL_CELL_TYPE, sportBuchungsJob));
        rowCells.add(new SportBuchungsJobCell(JOB_STATUS_CELL_TYPE, sportBuchungsJob));
        rowCells.add(new SportBuchungsJobCell(COUNTDOWN_CELL_TYPE, sportBuchungsJob));
        rowCells.add(new SportBuchungsJobCell(TERMIN_COUNTDOWN_CELL_TYPE, sportBuchungsJob));
        return rowCells;
    }


    public void removeSportBuchungsJob(SportBuchungsJob buchungsJob) {
        SportArt sportArt = buchungsJob.getSportArt();
        NavigableSet<SportBuchungsJob> sportBuchungsJobs = getSportBuchungsJobs(sportArt);
        sportBuchungsJobs.remove(buchungsJob);
        if (sportBuchungsJobs.isEmpty()) {
            sportArtWithJobs.remove(sportArt);
        }
        rebuildTableCells();
    }


    public SportBuchungsJob getSelectedSportBuchungsJob() {
        return getSelectedBuchungsJobCell().buchungsJob;
    }

    private SportBuchungsJobCell getSelectedBuchungsJobCell() {
        return getTableModel().getRow(getSelectedRow()).get(0);
    }


    @Override
    public Result handleKeyStroke(KeyStroke keyStroke) {
        Result result = super.handleKeyStroke(keyStroke);
        SportBuchungsJobCell selectedBuchungsJobCell = getSelectedBuchungsJobCell();
        if (!SPORT_ART_HEADER_CELL.equals(selectedBuchungsJobCell.cellType)) {
            return result;
        }
        switch (keyStroke.getKeyType()) {
        case ArrowUp:
            if (getSelectedRow() == 0) {
                setSelectedRow(Math.min(1, getSelectedRow()));
                return Result.MOVE_FOCUS_UP;
            }
            setSelectedRow(Math.max(getSelectedRow() - 1, 0));
            return result;

        case ArrowDown:
            setSelectedRow(Math.min(getSelectedRow() + 1, getTableModel().getRowCount()));
            return result;
        }
        return result;
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        if (getSelectedRow() == 0 && getTableModel().getRowCount() > 0) {
            setSelectedRow(1); // skip first SportArt header row
        }
    }


    private static class SportBuchungsJobTableCellRenderer extends DefaultTableCellRenderer<SportBuchungsJobCell> {

        @Override
        public TerminalSize getPreferredSize(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell,
                int columnIndex, int rowIndex) {
            switch (cell.cellType) {
            case SPORT_ART_HEADER_CELL:
                if (columnIndex > 0) {
                    return TerminalSize.ZERO;
                }
                String sportArtName = cell.buchungsJob.getSportArt().getName();
                return new TerminalSize(getColumnWidth(sportArtName), 1);
            case JOB_LABEL_CELL_TYPE:
                String sportBuchungsJobText = toSportBuchungsJobText(cell.buchungsJob);
                return new TerminalSize(getColumnWidth(sportBuchungsJobText), 1);
            case JOB_STATUS_CELL_TYPE:
                return new TerminalSize(SportBuchungsVersuchStatus.versuchStatusMaxLength(), 1);

            case COUNTDOWN_CELL_TYPE:
            case TERMIN_COUNTDOWN_CELL_TYPE:
                return new TerminalSize(4, 1);

            default:
                throw new IllegalArgumentException("unknown cellType: " + cell.cellType);
            }
        }


        @Override
        protected void render(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell, int columnIndex,
                int rowIndex, boolean isSelected, TextGUIGraphics graphics) {
            switch (cell.cellType) {
            case SPORT_ART_HEADER_CELL:
                renderSportArtHeaderCell(table, cell, columnIndex, rowIndex, isSelected, graphics);
                break;

            case JOB_LABEL_CELL_TYPE:
                renderJobLabelCell(table, cell, columnIndex, rowIndex, isSelected, graphics);
                break;

            case JOB_STATUS_CELL_TYPE:
                renderJobStatusCell(table, cell, columnIndex, rowIndex, isSelected, graphics);
                break;

            case COUNTDOWN_CELL_TYPE:
                renderBuchungsCountdownCell(table, cell, columnIndex, rowIndex, isSelected, graphics);
                break;

            case TERMIN_COUNTDOWN_CELL_TYPE:
                renderBuchungsCountdownCell(table, cell, columnIndex, rowIndex, isSelected, graphics);
                break;
            }
        }


        private void renderJobStatusCell(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell, int columnIndex,
                int rowIndex, boolean isSelected, TextGUIGraphics graphics) {
            SportBuchungsVersuchStatus lastBuchungsVersuchStatus = cell.buchungsJob.getLastBuchungsVersuchStatus();
            String text = lastBuchungsVersuchStatus.toString();
            text = TerminalTextUtils.fitString(text, graphics.getSize().getColumns());
            graphics.setForegroundColor(toForegroundColor(lastBuchungsVersuchStatus));
            graphics.setBackgroundColor(toBackgroundColor(lastBuchungsVersuchStatus));
            graphics.putString(TOP_LEFT_CORNER, text);
        }


        private void renderJobLabelCell(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell, int columnIndex,
                int rowIndex, boolean isSelected, TextGUIGraphics graphics) {
            String text = toSportBuchungsJobText(cell.buchungsJob);
            text = TerminalTextUtils.fitString(text, graphics.getSize().getColumns());
            graphics.putString(TOP_LEFT_CORNER, text);
        }

        private void renderSportArtHeaderCell(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell,
                int columnIndex, int rowIndex, boolean isSelected, TextGUIGraphics graphics) {
            if (columnIndex > 0) {
                return;
            }
            String sportArtName = cell.buchungsJob.getSportArt().getName();
            sportArtName = TerminalTextUtils.fitString(sportArtName, graphics.getSize().getColumns());
            graphics.putString(TOP_LEFT_CORNER, sportArtName);
        }

        private void renderBuchungsCountdownCell(Table<SportBuchungsJobCell> table, SportBuchungsJobCell cell,
                int columnIndex, int rowIndex, boolean isSelected, TextGUIGraphics graphics) {
            CountdownProgressBar buchungCountdownBar = new CountdownProgressBar();
            buchungCountdownBar.setPreferredWidth(4);
            buchungCountdownBar.startCountdown(cell.buchungsJob.getBevorstehenderBuchungsVersuch());
            buchungCountdownBar.onAdded(table.getParent());
            buchungCountdownBar.draw(graphics);
        }


        private String toSportBuchungsJobText(SportBuchungsJob buchungsJob) {
            String kursnummer = buchungsJob.getSportAngebot().getKursnummer();
            SportTermin sportTermin = buchungsJob.getSportTermin();
            return " " + kursnummer + " " + sportTermin.formatTerminZeitraum();
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

    }


    @Data
    public class SportBuchungsJobCell {

        private final SportBuchungsJobCellType cellType;
        private final SportBuchungsJob buchungsJob;

    }


    public enum SportBuchungsJobCellType {
        SPORT_ART_HEADER_CELL, //
        JOB_LABEL_CELL_TYPE, //
        JOB_STATUS_CELL_TYPE, //
        COUNTDOWN_CELL_TYPE, //
        TERMIN_COUNTDOWN_CELL_TYPE;
    }


}
