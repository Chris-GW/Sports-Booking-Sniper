package de.chrisgw.sportbookingsniper.gui.bind;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;


public class ConcealableComponent extends AbstractComposite<ConcealableComponent> {

    private TerminalSize occupiedSize = TerminalSize.ZERO;
    private boolean visible = true;


    public ConcealableComponent() {
        super();
    }

    public ConcealableComponent(Component component) {
        super();
        setComponent(component);
    }


    public ConcealableComponent occupiedSize() {
        return occupiedSize(TerminalSize.ONE);
    }

    public ConcealableComponent occupiedSize(TerminalSize occupiedSize) {
        this.occupiedSize = occupiedSize;
        invalidate();
        return this;
    }


    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            invalidate();
        }
    }

    public boolean isVisible() {
        return getComponent() != null && visible;
    }


    @Override
    protected ComponentRenderer<ConcealableComponent> createDefaultRenderer() {
        return new DefaultHideableComponentRenderer();
    }


    public static class DefaultHideableComponentRenderer implements ComponentRenderer<ConcealableComponent> {

        @Override
        public TerminalSize getPreferredSize(ConcealableComponent concealableComponent) {
            if (concealableComponent.isVisible()) {
                return concealableComponent.getComponent().getPreferredSize();
            } else {
                return concealableComponent.occupiedSize;
            }
        }

        @Override
        public void drawComponent(TextGUIGraphics graphics, ConcealableComponent concealableComponent) {
            if (concealableComponent.isVisible()) {
                concealableComponent.getComponent().draw(graphics);
            } else {
                new EmptySpace(concealableComponent.occupiedSize).draw(graphics);
            }
        }

    }


}
