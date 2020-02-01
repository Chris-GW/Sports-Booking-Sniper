package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class WatchableTextBox extends TextBox {


    private final List<Listener> listeners = new CopyOnWriteArrayList<>();


    public WatchableTextBox() {
        super();
    }

    public WatchableTextBox(String initialContent) {
        super(initialContent);
    }

    public WatchableTextBox(String initialContent, Style style) {
        super(initialContent, style);
    }

    public WatchableTextBox(TerminalSize preferredSize) {
        super(preferredSize);
    }

    public WatchableTextBox(TerminalSize preferredSize, Style style) {
        super(preferredSize, style);
    }

    public WatchableTextBox(TerminalSize preferredSize, String initialContent) {
        super(preferredSize, initialContent);
    }

    public WatchableTextBox(TerminalSize preferredSize, String initialContent, Style style) {
        super(preferredSize, initialContent, style);
    }


    public WatchableTextBox addListener(Listener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
        return this;
    }

    public WatchableTextBox removeListener(Listener listener) {
        listeners.remove(listener);
        return this;
    }


    @Override
    public synchronized WatchableTextBox setText(String text) {
        String previousText = getLineCount() > 0 ? getText() : null;
        super.setText(text);
        if (listeners != null && !listeners.isEmpty()) {
            runOnGUIThreadIfExistsOtherwiseRunDirect(() -> {
                for (Listener listener : listeners) {
                    listener.onChanged(text, previousText);
                }
            });
        }
        return this;
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        String previousText = getText();
        switch (keyStroke.getKeyType()) {
        case Character:
        case Backspace:
        case Delete:
        case Enter:
            Result result = super.handleKeyStroke(keyStroke);
            String text = getText();
            runOnGUIThreadIfExistsOtherwiseRunDirect(() -> {
                listeners.forEach(listener -> listener.onChanged(text, previousText));
            });
            return result;
        default:
            return super.handleKeyStroke(keyStroke);
        }
    }


    /**
     * Listener interface that can be used to catch user events on the text box
     */
    public interface Listener {

        /**
         * This method is called whenever the user changes the input in the text box
         *
         * @param text         of the input which is currently entered
         * @param previousText of the input which was previously entered
         */
        void onChanged(String text, String previousText);

    }

}
