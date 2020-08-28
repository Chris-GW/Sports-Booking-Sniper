package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;


public class SportBookingComponent extends Panel implements WindowListener {

    protected final ApplicationStateDao applicationStateDao;
    protected final Window window;

    @Getter
    private final String title;

    @Getter
    private final KeyType shortKeyType;

    @Getter
    private boolean visible = true;


    public SportBookingComponent(ApplicationStateDao applicationStateDao, Window window) {
        this(applicationStateDao, window, null, null);
    }

    public SportBookingComponent(ApplicationStateDao applicationStateDao, Window window, String title) {
        this(applicationStateDao, window, title, null);
    }

    public SportBookingComponent(ApplicationStateDao applicationStateDao, Window window, String title,
            KeyType shortKeyType) {
        super();
        this.applicationStateDao = applicationStateDao;
        this.window = window;
        this.title = title;
        this.shortKeyType = shortKeyType;
    }


    public synchronized void focus() {
        Interactable interactable = this.nextFocus(null);
        window.setFocusedInteractable(interactable);
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        window.addWindowListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        window.removeWindowListener(this);
    }


    @Override
    public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
        // no-op
    }

    @Override
    public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
        // no-op
    }

    @Override
    public void onInput(Window baseWindow, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        if (keyStroke.isShiftDown() && keyStroke.getKeyType().equals(shortKeyType)) {
            setVisible(!isVisible());
            deliverEvent.set(true);
        } else if (keyStroke.getKeyType().equals(shortKeyType)) {
            focus();
            deliverEvent.set(true);
        }
    }

    @Override
    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
        // no-op
    }


    @Override
    protected ComponentRenderer<Panel> createDefaultRenderer() {
        return new DefaultPanelRenderer() {

            @Override
            public TerminalSize getPreferredSize(Panel component) {
                if (!visible) {
                    return new TerminalSize(title.length(), 2);
                }
                return super.getPreferredSize(component);
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, Panel panel) {
                if (visible) {
                    super.drawComponent(graphics, panel);
                } else {
                    graphics.applyThemeStyle(getThemeDefinition().getNormal());
                    graphics.putString(TerminalPosition.TOP_LEFT_CORNER, title);
                }
            }

        };
    }

    @Override
    protected void onAfterDrawing(TextGUIGraphics graphics) {
        super.onAfterDrawing(graphics);
        // draw sizeInfo bottomRight
        //        String sizeInfo = String.format("[%s/%s]", getPreferredSize(), getSize());
        //        TerminalPosition bottomRight = new TerminalPosition(0, getSize().getRows() - 1);
        //        graphics.setForegroundColor(ANSI.BLACK).setBackgroundColor(ANSI.GREEN).putString(bottomRight, sizeInfo);
    }


    @Override
    public synchronized SportBookingComponent addTo(Panel panel) {
        super.addTo(panel);
        return self();
    }

    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }

    @Override
    protected SportBookingComponent self() {
        return this;
    }

}
