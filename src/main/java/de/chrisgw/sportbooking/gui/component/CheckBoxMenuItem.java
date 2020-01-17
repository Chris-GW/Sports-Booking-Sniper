package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.CheckBox.Listener;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.menu.MenuItem;

import java.util.Arrays;


public class CheckBoxMenuItem extends MenuItem {

    private CheckBox checkBox;


    public CheckBoxMenuItem(String label) {
        super(label);
        checkBox = new CheckBox(label);
        checkBox.setTheme(getCheckBoxTheme());
    }

    public CheckBoxMenuItem(String label, Listener... checkBoxListeners) {
        this(label);
        if (checkBoxListeners != null) {
            Arrays.stream(checkBoxListeners).forEachOrdered(this::addListener);
        }
    }


    @Override
    public String getLabel() {
        return checkBox.getLabel();
    }

    public CheckBoxMenuItem setLabel(String label) {
        checkBox.setLabel(label);
        return this;
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
        return false;
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
        return new InteractableRenderer<MenuItem>() {

            @Override
            public TerminalPosition getCursorLocation(MenuItem component) {
                return checkBox.getRenderer().getCursorLocation(checkBox);
            }

            @Override
            public TerminalSize getPreferredSize(MenuItem component) {
                return checkBox.getRenderer().getPreferredSize(checkBox);
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, MenuItem component) {
                checkBox.getRenderer().drawComponent(graphics, checkBox);
            }

        };
    }


    private DelegatingTheme getCheckBoxTheme() {
        return new DelegatingTheme(getTheme()) {

            @Override
            public ThemeDefinition getDefinition(Class<?> clazz) {
                if (clazz.equals(CheckBox.class)) {
                    return new DelegatingThemeDefinition(super.getDefinition(clazz)) {

                        @Override
                        public ThemeStyle getActive() {
                            return new DefaultMutableThemeStyle(super.getActive()).setForeground(ANSI.DEFAULT)
                                    .setBackground(ANSI.GREEN);
                        }

                        @Override
                        public ThemeStyle getPreLight() {
                            return new DefaultMutableThemeStyle(super.getActive()).setForeground(ANSI.DEFAULT)
                                    .setBackground(ANSI.GREEN);
                        }

                        @Override
                        public ThemeStyle getSelected() {
                            return new DefaultMutableThemeStyle(super.getSelected()).setForeground(ANSI.WHITE)
                                    .setBackground(ANSI.DEFAULT);
                        }

                    };
                }
                return super.getDefinition(clazz);
            }
        };
    }

}
