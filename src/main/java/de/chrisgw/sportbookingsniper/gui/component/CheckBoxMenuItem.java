package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.CheckBox.Listener;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.menu.MenuItem;

import java.util.Arrays;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;


public class CheckBoxMenuItem extends MenuItem {

    private CheckBox checkBox;


    public CheckBoxMenuItem(String label) {
        super(label);
        checkBox = new CheckBox();
    }

    public CheckBoxMenuItem(String label, Listener... checkBoxListeners) {
        this(label);
        if (checkBoxListeners != null) {
            Arrays.stream(checkBoxListeners).forEachOrdered(this::addListener);
        }
    }


    public CheckBox addListener(Listener listener) {
        return checkBox.addListener(listener);
    }

    public CheckBox removeListener(Listener listener) {
        return checkBox.removeListener(listener);
    }


    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }


    @Override
    protected boolean onActivated() {
        setChecked(!isChecked());
        return false; // never close menu item popup window
    }


    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        checkBox.onEnterFocus(direction, previouslyInFocus);
    }

    @Override
    protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
        super.afterLeaveFocus(direction, nextInFocus);
        checkBox.onLeaveFocus(direction, nextInFocus);
    }


    @Override
    protected InteractableRenderer<MenuItem> createDefaultRenderer() {
        final InteractableRenderer<MenuItem> menuItemRenderer = super.createDefaultRenderer();
        return new InteractableRenderer<MenuItem>() {

            @Override
            public TerminalPosition getCursorLocation(MenuItem component) {
                return checkBox.getRenderer().getCursorLocation(checkBox);
            }

            @Override
            public TerminalSize getPreferredSize(MenuItem component) {
                TerminalSize checkBoxPreferredSize = checkBox.getPreferredSize();
                TerminalSize menuItemPreferredSize = menuItemRenderer.getPreferredSize(component);
                int columns = checkBoxPreferredSize.getColumns() + menuItemPreferredSize.getColumns();
                return TerminalSize.ONE.withColumns(columns);
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, MenuItem component) {
                TerminalSize checkBoxSize = checkBox.getPreferredSize();
                TextGUIGraphics checkBoxGraphics = graphics.newTextGraphics(TOP_LEFT_CORNER, checkBoxSize);
                drawCheckBox(checkBoxGraphics, component);

                TerminalPosition topLeftCorner = TOP_LEFT_CORNER.withRelativeColumn(checkBoxSize.getColumns());
                TerminalSize menuItemSize = graphics.getSize().withRelativeColumns(-checkBoxSize.getColumns());
                TextGUIGraphics menuItemGraphics = graphics.newTextGraphics(topLeftCorner, menuItemSize);
                menuItemRenderer.drawComponent(menuItemGraphics, component);
            }

            private void drawCheckBox(TextGUIGraphics graphics, MenuItem component) {
                ThemeDefinition themeDefinition = component.getThemeDefinition();
                if (component.isFocused()) {
                    graphics.applyThemeStyle(themeDefinition.getSelected());
                } else {
                    graphics.applyThemeStyle(themeDefinition.getNormal());
                }
                graphics.fill(' ');
                graphics.setCharacter(0, 0, themeDefinition.getCharacter("LEFT_BRACKET", '['));
                graphics.setCharacter(2, 0, themeDefinition.getCharacter("RIGHT_BRACKET", ']'));
                graphics.setCharacter(3, 0, ' ');

                if (component.isFocused()) {
                    ThemeDefinition checkBoxThemeDefinition = checkBox.getThemeDefinition();
                    graphics.applyThemeStyle(checkBoxThemeDefinition.getSelected());
                } else {
                    graphics.applyThemeStyle(themeDefinition.getNormal());
                }
                graphics.setCharacter(1, 0, (isChecked() ? themeDefinition.getCharacter("MARKER", 'x') : ' '));
            }

        };
    }

}
