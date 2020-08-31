package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;


@Data
@Builder
public class KeyStrokeAction extends WindowListenerAdapter implements Runnable {

    private final String name;
    private final KeyStroke keyStroke;
    private final Runnable action;


    @Override
    public void run() {
        if (action != null) {
            action.run();
        }
    }


    @Override
    public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        onUnhandledInput(basePane, keyStroke, deliverEvent);
    }

    @Override
    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        if (isKeyStrokeAction(keyStroke)) {
            run();
            hasBeenHandled.set(true);
        }
    }

    private boolean isKeyStrokeAction(KeyStroke keyStroke) {
        return this.keyStroke.equals(keyStroke);
    }


    @Override
    public String toString() {
        return name + " " + keyStroke;
    }

}
