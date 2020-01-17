package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.gui.SportBookingGui;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;


@Data
public class MultipleWindowNavigator extends WindowListenerAdapter {

    private final SportBookingGui sportBookingGui;
    private final Set<Pair<KeyType, Window>> windowShortKeyTypeMapping = new LinkedHashSet<>();
    private final Map<Window, Interactable> previousFocusedInteractable = new LinkedHashMap<>();
    private final List<WindowConnection> windowConnections = new ArrayList<>();


    public MultipleWindowNavigator addManagedWindow(Window window, KeyStroke windowShortKeyStroke) {
        KeyType keyType = windowShortKeyStroke != null ? windowShortKeyStroke.getKeyType() : null;
        windowShortKeyTypeMapping.add(Pair.of(keyType, window));
        previousFocusedInteractable.put(window, window.getFocusedInteractable());
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


    public Optional<KeyType> findShortKeyTypeForWindow(Window window) {
        return windowShortKeyTypeMapping.stream()
                .filter(keyTypeWindowPair -> keyTypeWindowPair.getRight().equals(window))
                .findAny()
                .map(Pair::getLeft);
    }

    public Optional<Window> findWindowForShortKeyType(KeyStroke keyStroke) {
        return windowShortKeyTypeMapping.stream()
                .filter(keyTypeWindowPair -> Objects.nonNull(keyTypeWindowPair.getLeft()))
                .filter(keyTypeWindowPair -> keyTypeWindowPair.getLeft().equals(keyStroke.getKeyType()))
                .findAny()
                .map(Pair::getRight);
    }


    @Override
    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        Window windowForShortKey = findWindowForShortKeyType(keyStroke).orElse(null);
        if (windowForShortKey != null && keyStroke.isShiftDown()) {
            toggleVisible(windowForShortKey);
            deliverEvent.set(false);
        } else if (windowForShortKey != null) {
            windowForShortKey.setVisible(true);
            setActiveWindow(windowForShortKey);
            deliverEvent.set(false);
        } else if (keyStroke.isCtrlDown())
            findNextWindowConnection(window, keyStroke.getKeyType()).ifPresent(nextWindow -> {
                setActiveWindow(nextWindow);
                deliverEvent.set(false);
            });
    }


    private void toggleVisible(Window windowForShortKey) {
        setWindowVisible(windowForShortKey, !windowForShortKey.isVisible());
    }


    @Override
    public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        findNextWindowConnection(window, keyStroke.getKeyType()).ifPresent(nextWindow -> {
            setActiveWindow(nextWindow);
            hasBeenHandled.set(true);
        });
    }


    private Optional<Window> findNextWindowConnection(Window window, KeyType keyType) {
        return windowConnections.stream()
                .filter(windowConnection -> windowConnection.matchesNavigation(window, keyType))
                .findAny()
                .map(WindowConnection::getOtherWindow)
                .flatMap(otherWindow -> {
                    if (otherWindow.isVisible()) {
                        return Optional.of(otherWindow);
                    } else {
                        return findNextWindowConnection(otherWindow, keyType);
                    }
                });
    }


    public void setWindowVisible(Window window, boolean visible) {
        window.setVisible(visible);
        if (!window.isVisible() && sportBookingGui.getTextGUI().getActiveWindow().equals(window)) {
            Stream.of(KeyType.ArrowUp, KeyType.ArrowLeft, KeyType.ArrowDown, KeyType.ArrowRight)
                    .map(keyType -> findNextWindowConnection(window, keyType))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .ifPresent(this::setActiveWindow);
        }
    }


    public void setActiveWindow(Window window) {
        Window previousActiveWindow = sportBookingGui.getTextGUI().getActiveWindow();
        previousFocusedInteractable.put(previousActiveWindow, previousActiveWindow.getFocusedInteractable());
        previousActiveWindow.setFocusedInteractable(null);

        sportBookingGui.getTextGUI().setActiveWindow(window);
        window.setFocusedInteractable(previousFocusedInteractable.get(window));
    }

    public Window getActiveWindow() {
        return sportBookingGui.getTextGUI().getActiveWindow();
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
