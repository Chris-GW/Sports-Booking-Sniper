package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportbookingsniper.angebot.SportArt;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.gui.state.SportBuchungJobListener;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.BiConsumer;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;
import static java.util.Comparator.comparing;


@RequiredArgsConstructor
public class SportBuchungsJobListBox extends AbstractListBox<SportBuchungsJobListItem, SportBuchungsJobListBox>
        implements SportBuchungJobListener {

    private final NavigableMap<SportArt, NavigableSet<SportBuchungsJobListItem>> sportArtWithJobs = new TreeMap<>();
    private final BiConsumer<SportBuchungsJobListBox, SportBuchungsJob> selectAction;


    @Override
    public synchronized SportBuchungsJobListBox addItem(SportBuchungsJobListItem item) {
        SportBuchungsJob buchungsJob = item.getSportBuchungsJob();
        int selectedIndex = getSelectedIndex();
        SportArt sportArt = buchungsJob.getSportArt();
        clearItems();
        sportArtWithJobs.computeIfAbsent(sportArt, this::newSportJobList).add(item);
        sportArtWithJobs.values().stream().flatMap(Collection::stream).forEachOrdered(super::addItem);
        setSelectedIndex(selectedIndex);
        return this;
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob buchungsJob) {
        addItem(new SportBuchungsJobListItem(buchungsJob));
    }

    private NavigableSet<SportBuchungsJobListItem> newSportJobList(SportArt sportArt) {
        return new TreeSet<>(
                comparing(SportBuchungsJobListItem::getSportBuchungsJob, comparing(SportBuchungsJob::getSportTermin)));
    }


    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        invalidate();
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {

    }


    @Override
    public Result handleKeyStroke(KeyStroke keyStroke) {
        if (isKeyboardActivationStroke(keyStroke)) {
            selectAction.accept(this, getSelectedItem().getSportBuchungsJob());
            return Result.HANDLED;
        }
        return super.handleKeyStroke(keyStroke);
    }


    @Override
    protected InteractableRenderer<SportBuchungsJobListBox> createDefaultRenderer() {
        return new SportBuchungsJobListBoxRenderer();
    }


    public static class SportBuchungsJobListBoxRenderer implements InteractableRenderer<SportBuchungsJobListBox> {

        private final ScrollBar verticalScrollBar;
        private int scrollTopIndex;


        public SportBuchungsJobListBoxRenderer() {
            this.verticalScrollBar = new ScrollBar(Direction.VERTICAL);
            this.scrollTopIndex = 0;
        }


        @Override
        public TerminalPosition getCursorLocation(SportBuchungsJobListBox listBox) {
            return null;
        }

        @Override
        public TerminalSize getPreferredSize(SportBuchungsJobListBox listBox) {
            int neededColumnsForScrollBar = 1;
            List<ListBoxRowRenderer> renderedRows = rowRendererList(listBox);
            return renderedRows.stream()
                    .map(ListBoxRowRenderer::getPreferredSize)
                    .reduce(TerminalSize::max)
                    .map(terminalSize -> terminalSize.withRelativeColumns(16 + 16))
                    .orElse(TerminalSize.ONE)
                    .withRelativeColumns(neededColumnsForScrollBar);
        }


        @Override
        public void drawComponent(TextGUIGraphics graphics, SportBuchungsJobListBox listBox) {
            ThemeDefinition themeDefinition = listBox.getTheme().getDefinition(AbstractListBox.class);
            graphics.applyThemeStyle(themeDefinition.getNormal()).fill(' ');

            int componentHeight = graphics.getSize().getRows();
            int selectedIndex = listBox.getSelectedIndex();
            List<ListBoxRowRenderer> rows = rowRendererList(listBox);

            if (selectedIndex != -1) {
                ListBoxRowRenderer itemRow = findRow(rows, selectedIndex);
                int selectedRowIndex = itemRow.getRowIndex();
                if (selectedIndex == 0) {
                    scrollTopIndex = 0;
                } else if (selectedRowIndex < scrollTopIndex) {
                    scrollTopIndex = selectedRowIndex;
                } else if (selectedRowIndex >= scrollTopIndex + componentHeight) {
                    scrollTopIndex = selectedRowIndex - componentHeight + 1;
                }
            }

            // Do we need to recalculate the scroll position?
            // This code would be triggered by resizing the window when the scroll
            // position is at the bottom
            if (rows.size() > componentHeight && rows.size() - scrollTopIndex < componentHeight) {
                scrollTopIndex = rows.size() - componentHeight;
                if (scrollTopIndex == 1) {
                    scrollTopIndex = 0;
                }
            }
            listBox.scrollOffset = new TerminalPosition(0, -scrollTopIndex);

            TerminalPosition rowPosition = TerminalPosition.TOP_LEFT_CORNER;
            TerminalSize rowSize = graphics.getSize().withRows(1);
            List<ListBoxRowRenderer> visibleRows = rows.subList(scrollTopIndex,
                    Math.min(scrollTopIndex + componentHeight, rows.size()));
            for (ListBoxRowRenderer rowRenderer : visibleRows) {
                rowRenderer.drawComponent(graphics.newTextGraphics(rowPosition, rowSize));
                rowPosition = rowPosition.withRelativeRow(1);
            }

            graphics.applyThemeStyle(themeDefinition.getNormal());
            if (rows.size() > componentHeight) {
                verticalScrollBar.onAdded(listBox.getParent());
                verticalScrollBar.setViewSize(componentHeight);
                verticalScrollBar.setScrollMaximum(rows.size());
                verticalScrollBar.setScrollPosition(scrollTopIndex);
                TerminalPosition topRightCorner = new TerminalPosition(graphics.getSize().getColumns() - 1, 0);
                TerminalSize oneVerticalColumnSize = new TerminalSize(1, graphics.getSize().getRows());
                verticalScrollBar.draw(graphics.newTextGraphics(topRightCorner, oneVerticalColumnSize));
            }
        }


        public List<ListBoxRowRenderer> rowRendererList(SportBuchungsJobListBox listBox) {
            List<ListBoxRowRenderer> rowRendererList = new ArrayList<>();
            int rowIndex = 0;
            int itemIndex = 0;
            for (SportArt sportArt : listBox.sportArtWithJobs.navigableKeySet()) {
                rowRendererList.add(new ListBoxRowRenderer(rowIndex++, itemIndex, true, listBox));
                for (SportBuchungsJobListItem listItem : listBox.sportArtWithJobs.get(sportArt)) {
                    rowRendererList.add(new ListBoxRowRenderer(rowIndex++, itemIndex++, false, listBox));
                }
            }
            return rowRendererList;
        }

        public ListBoxRowRenderer findRow(List<ListBoxRowRenderer> rows, int itemIndex) {
            return rows.stream()
                    .filter(listBoxRowRenderer -> listBoxRowRenderer.isAt(itemIndex))
                    .findAny()
                    .orElse(null);
        }


        @Data
        private static class ListBoxRowRenderer implements ComponentRenderer<SportBuchungsJobListItem> {

            private final int rowIndex;
            private final int itemIndex;
            private final boolean isSportArtRow;
            private final SportBuchungsJobListBox listBox;


            public TerminalSize getPreferredSize() {
                return getPreferredSize(getComponent());
            }

            @Override
            public TerminalSize getPreferredSize(SportBuchungsJobListItem component) {
                return component.getPreferredSize();
            }


            public void drawComponent(TextGUIGraphics graphics) {
                drawComponent(graphics, getComponent());
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, SportBuchungsJobListItem component) {
                component = getComponent();
                component.onAdded(listBox.getParent());
                SportBuchungsJob buchungsJob = getSportBuchungsJob();
                if (isSportArtRow) {
                    graphics.applyThemeStyle(listBox.getThemeDefinition().getNormal());
                    String sportArtName = buchungsJob.getSportArt().getName();
                    graphics.putString(TOP_LEFT_CORNER, sportArtName);
                    return;
                }
                component.setSelectedAndFocused(itemIndex == listBox.getSelectedIndex() && listBox.isFocused());
                component.draw(graphics);
            }


            public boolean isAt(int itemIndex) {
                return !isSportArtRow && this.itemIndex == itemIndex;
            }

            public SportBuchungsJob getSportBuchungsJob() {
                return getComponent().getSportBuchungsJob();
            }

            private SportBuchungsJobListItem getComponent() {
                return listBox.getItemAt(itemIndex);
            }


        }

    }

}
