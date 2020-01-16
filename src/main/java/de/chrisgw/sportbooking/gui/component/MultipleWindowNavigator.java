package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Data
public class MultipleWindowNavigator extends WindowListenerAdapter {

    private final WindowBasedTextGUI windowTextGUI;
    private final Map<KeyType, Window> windowShortKeyStrokeMapping = new HashMap<>();
    private final Map<Window, Interactable> previousFocusedInteractable = new HashMap<>();
    private final List<WindowConnection> windowConnections = new ArrayList<>();


    public MultipleWindowNavigator addManagedWindow(Window window, KeyStroke windowShortKeyStroke) {
        windowShortKeyStrokeMapping.put(windowShortKeyStroke.getKeyType(), window);
        previousFocusedInteractable.put(window, window.getFocusedInteractable());
        windowTextGUI.addWindow(window);
        window.setFocusedInteractable(null);
        window.addWindowListener(this);
        return this;
    }


    public MultipleWindowNavigator connectWindowsHorizontally(Window leftWindow, Window rightWindow) {
        windowConnections.add(new WindowConnection(leftWindow, rightWindow, KeyType.ArrowRight));
        windowConnections.add(new WindowConnection(rightWindow, leftWindow, KeyType.ArrowLeft));
        return this;
    }

    public MultipleWindowNavigator connectWindowsVertically(Window topWindow, Window bottomWindow) {
        windowConnections.add(new WindowConnection(topWindow, bottomWindow, KeyType.ArrowDown));
        windowConnections.add(new WindowConnection(bottomWindow, topWindow, KeyType.ArrowUp));
        return this;
    }


    @Override
    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        Window focuseWindow = windowShortKeyStrokeMapping.get(keyStroke.getKeyType());
        if (focuseWindow != null && keyStroke.isShiftDown()) {
            focuseWindow.setVisible(!focuseWindow.isVisible());
            deliverEvent.set(false);
        } else if (focuseWindow != null) {
            focuseWindow.setVisible(true);
            setActiveWindow(focuseWindow);
            deliverEvent.set(false);
        } else if (keyStroke.isCtrlDown())
            findNextWindowConnection(window, keyStroke).ifPresent(nextWindow -> {
                setActiveWindow(nextWindow);
                deliverEvent.set(false);
            });
    }


    @Override
    public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        findNextWindowConnection(window, keyStroke).ifPresent(nextWindow -> {
            setActiveWindow(nextWindow);
            hasBeenHandled.set(true);
        });
    }


    private Optional<Window> findNextWindowConnection(Window window, KeyStroke keyStroke) {
        return windowConnections.stream()
                .filter(windowConnection -> windowConnection.matchesNavigation(window, keyStroke.getKeyType()))
                .findAny()
                .map(WindowConnection::getOtherWindow)
                .flatMap(otherWindow -> {
                    if (otherWindow.isVisible()) {
                        return Optional.of(otherWindow);
                    } else {
                        return findNextWindowConnection(otherWindow, keyStroke);
                    }
                });
    }


    public void setActiveWindow(Window window) {
        Window previousActiveWindow = windowTextGUI.getActiveWindow();
        previousFocusedInteractable.put(previousActiveWindow, previousActiveWindow.getFocusedInteractable());
        previousActiveWindow.setFocusedInteractable(null);

        windowTextGUI.setActiveWindow(window);
        window.setFocusedInteractable(previousFocusedInteractable.get(window));
    }

    public Window getActiveWindow() {
        return windowTextGUI.getActiveWindow();
    }


    @Data
    public static class WindowConnection {

        private final Window window;
        private final Window otherWindow;
        private final KeyType arrowDirection;


        public boolean matchesNavigation(Window window, KeyType keyType) {
            return this.window.equals(window) && this.arrowDirection.equals(keyType);
        }

    }

}
