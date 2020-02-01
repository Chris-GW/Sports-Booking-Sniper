package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ComboBox.Listener;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.input.KeyStroke;
import de.chrisgw.sportbooking.model.SportArt;
import de.chrisgw.sportbooking.model.SportKatalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;


public class SportArtComboBox extends ComboBox<SportArt> implements Listener {

    private SearchablePopupWindow popupWindow;

    public SportArtComboBox() {
        super("", Collections.emptyList());
        this.popupWindow = null;
        setReadOnly(true);
        setDropDownNumberOfRows(20);
        addListener(this);
    }

    public SportArtComboBox(SportKatalog sportKatalog) {
        super("", sportKatalog.getSportArten());
        this.popupWindow = null;
        setReadOnly(true);
        setDropDownNumberOfRows(20);
        addListener(this);
    }

    public SportArtComboBox withKatalog(SportKatalog sportKatalog) {
        sportKatalog.getSportArten().forEach(this::addItem);
        return this;
    }


    @Override
    public void onSelectionChanged(int selectedIndex, int previousSelection) {
        if (selectedIndex == 0) {

        }
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
            ActionListBox listBox = new ActionListBox(SportArtComboBox.this.getSize().withRows(getItemCount()));
            for (int i = 0; i < getItemCount(); i++) {
                SportArt item = getItem(i);
                final int index = i;
                listBox.addItem(item.toString(), () -> {
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
                    SportArt item = getItem(i);
                    if (!item.getName().toLowerCase().contains(searchText)) {
                        continue;
                    }
                    final int index = i;
                    listBox.addItem(item.toString(), () -> {
                        setSelectedIndex(index);
                        close();
                    });
                }
            };
        }

        @Override
        public synchronized Theme getTheme() {
            return SportArtComboBox.this.getTheme();
        }

    }

}
