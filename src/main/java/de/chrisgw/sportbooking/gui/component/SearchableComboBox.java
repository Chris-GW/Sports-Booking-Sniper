package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;


public class SearchableComboBox<V> extends ComboBox<V> {

    private Function<V, String> itemToString;
    private SearchablePopupWindow popupWindow;


    public SearchableComboBox(V... items) {
        this(Arrays.asList(items));
    }


    public SearchableComboBox(Collection<V> items) {
        this("", items);
    }

    public SearchableComboBox(String initialText, Collection<V> items) {
        super(initialText, items);
        this.popupWindow = null;
        this.itemToString = Objects::toString;
        setReadOnly(true);
        setDropDownNumberOfRows(20);
    }


    public SearchableComboBox<V> setItemToString(Function<V, String> itemToString) {
        this.itemToString = requireNonNull(itemToString);
        return this;
    }


    @Override
    protected synchronized void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
        if (popupWindow != null) {
            popupWindow.close();
            popupWindow = null;
        }
    }


    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        // need to use custom PopupWindow
        switch (keyStroke.getKeyType()) {
        case Insert:
        case Delete:
        case ArrowLeft:
        case ArrowRight:
        case Backspace:
        case Character:
            if (popupWindow != null) {
                popupWindow.searchTextBox.handleKeyStroke(keyStroke);
                return Result.HANDLED;
            }
            break;

        case ArrowDown:
            if (popupWindow != null) {
                popupWindow.listBox.handleKeyStroke(keyStroke);
                return Result.HANDLED;
            }
            return Result.MOVE_FOCUS_DOWN;

        case ArrowUp:
            if (popupWindow != null) {
                popupWindow.listBox.handleKeyStroke(keyStroke);
                return Result.HANDLED;
            }
            return Result.MOVE_FOCUS_UP;

        case PageUp:
        case PageDown:
        case Home:
        case End:
            if (popupWindow != null) {
                popupWindow.listBox.handleKeyStroke(keyStroke);
                return Result.HANDLED;
            }
            break;

        case Enter:
            if (popupWindow != null) {
                popupWindow.listBox.handleKeyStroke(keyStroke);
                popupWindow.close();
                popupWindow = null;
            } else {
                popupWindow = new SearchablePopupWindow();
                popupWindow.setPosition(toGlobal(new TerminalPosition(0, 1)));
                ((WindowBasedTextGUI) getTextGUI()).addWindow(popupWindow);
            }
            return Result.UNHANDLED;

        case Escape:
            if (popupWindow != null) {
                popupWindow.close();
                popupWindow = null;
                return Result.HANDLED;
            }
            break;

        default:
        }
        return super.handleKeyStroke(keyStroke);
    }


    private class SearchablePopupWindow extends BasicWindow {

        private final TextBox searchTextBox;
        private final ActionListBox listBox;


        public SearchablePopupWindow() {
            setHints(Arrays.asList(Hint.NO_FOCUS, Hint.FIXED_POSITION));
            searchTextBox = createSearchTextBox();
            listBox = createItemActionListBox();
            setComponent(Panels.vertical(searchTextBox, listBox));
            setFocusedInteractable(listBox);
        }

        private TextBox createSearchTextBox() {
            return new WatchableTextBox("Search: ").addListener(onSearchTextChanged())
                    .setValidationPattern(Pattern.compile("Search: .*"))
                    .setLayoutData(LinearLayout.createLayoutData(Alignment.Fill));
        }

        private ActionListBox createItemActionListBox() {
            ActionListBox listBox = new ActionListBox(SearchableComboBox.this.getSize().withRows(getItemCount()));
            for (int i = 0; i < getItemCount(); i++) {
                V item = getItem(i);
                final int index = i;
                listBox.addItem(itemToString.apply(item), () -> {
                    setSelectedIndex(index);
                    close();
                });
            }
            listBox.setSelectedIndex(getSelectedIndex());
            TerminalSize dropDownListPreferedSize = listBox.getPreferredSize();
            if (getDropDownNumberOfRows() > 0) {
                listBox.setPreferredSize(dropDownListPreferedSize.withRows(
                        Math.min(getDropDownNumberOfRows(), dropDownListPreferedSize.getRows())));
            }
            listBox.setLayoutData(LinearLayout.createLayoutData(Alignment.Fill));
            return listBox;
        }

        private WatchableTextBox.Listener onSearchTextChanged() {
            return (text, previousText) -> {
                String searchText = text.substring("Search: ".length()).toLowerCase();
                listBox.clearItems();
                for (int i = 0; i < getItemCount(); i++) {
                    V item = getItem(i);
                    if (!itemToString.apply(item).toLowerCase().contains(searchText)) {
                        continue;
                    }
                    final int index = i;
                    listBox.addItem(itemToString.apply(item), () -> {
                        setSelectedIndex(index);
                        close();
                    });
                }
            };
        }

        @Override
        public synchronized Theme getTheme() {
            return SearchableComboBox.this.getTheme();
        }

    }

}
