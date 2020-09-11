package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class ShortKeyRegistry implements WindowListener, TextGUI.Listener {

    private Map<KeyStroke, List<Runnable>> keyStrokeToActions = new HashMap<>();


    public ShortKeyRegistry registerAction(KeyStroke keyStroke, Runnable runnable) {
        keyStrokeToActions.computeIfAbsent(keyStroke, ks -> new CopyOnWriteArrayList<>()).add(runnable);
        return this;
    }

    public ShortKeyRegistry registerButtonAction(KeyStroke keyStroke, Button button) {
        registerAction(keyStroke, () -> {
            button.getBasePane().setFocusedInteractable(button);
            button.handleKeyStroke(new KeyStroke(KeyType.Enter));
        });
        return this;
    }


    public List<Runnable> removeAllActionsFor(KeyStroke keyStroke) {
        return keyStrokeToActions.remove(keyStroke);
    }

    public boolean removeAction(KeyStroke keyStroke, Runnable action) {
        List<Runnable> actions = getActionsFor(keyStroke);
        boolean remove = actions.remove(action);
        if (actions.isEmpty()) {
            keyStrokeToActions.remove(keyStroke);
        }
        return remove;
    }


    public List<Runnable> getActionsFor(KeyStroke keyStroke) {
        return keyStrokeToActions.getOrDefault(keyStroke, Collections.emptyList());
    }

    private boolean runActionsForKeyStroke(KeyStroke keyStroke) {
        List<Runnable> actions = getActionsFor(keyStroke);
        actions.forEach(Runnable::run);
        return !actions.isEmpty();
    }


    @Override
    public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
        return runActionsForKeyStroke(keyStroke);
    }


    @Override
    public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        deliverEvent.set(!runActionsForKeyStroke(keyStroke));
    }


    @Override
    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        hasBeenHandled.set(runActionsForKeyStroke(keyStroke));
    }


    @Override
    public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
        // no-op
    }

    @Override
    public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
        // no-op
    }

}
