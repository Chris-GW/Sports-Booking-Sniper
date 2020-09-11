package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;


@Builder
@RequiredArgsConstructor
public class KeyStrokeAction extends WindowListenerAdapter implements Runnable {

    @Getter
    private final String name;

    @Getter
    private final KeyStroke keyStroke;

    @Getter
    private final Runnable action;


    @Override
    public void run() {
        if (action != null) {
            action.run();
        }
    }


    @Override
    public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        if (isKeyStrokeAction(keyStroke)) {
            deliverEvent.set(false);
            run();
        }
    }

    public boolean isKeyStrokeAction(KeyStroke keyStroke) {
        return this.keyStroke.equals(keyStroke);
    }


    public Button asActionButton() {
        return new Button(toString(), action);
    }


    @Override
    public String toString() {
        return name + " " + keyStroke;
    }

}
