package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.AbstractListBox;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Base class for several list box implementations, this will handle things like list of items and the scrollbar.
 *
 * @param <T> Should always be itself, see {@code AbstractComponent}
 * @param <V> Type of items this list box contains
 * @author Chris-GW
 */
public abstract class AbstractWatchableListBox<V, T extends AbstractWatchableListBox<V, T>>
        extends AbstractListBox<V, T> {

    /**
     * Listener interface that can be attached to the {@code AbstractWatchableListBox} in order to be notified
     * on changed item selection
     */
    public interface SelectedItemListener<V> {

        /**
         * Called by the {@code AbstractWatchableListBox} when the user changes which item is selected
         *
         * @param item selected item
         */
        void onItemSelected(int index, V item);

    }


    private final List<SelectedItemListener<V>> itemListeners = new CopyOnWriteArrayList<>();


    public T addSelectedItemListener(SelectedItemListener<V> selectedItemListener) {
        itemListeners.add(selectedItemListener);
        return self();
    }

    public T removeSelectedItemListener(SelectedItemListener<V> selectedItemListener) {
        itemListeners.remove(selectedItemListener);
        return self();
    }


    @Override
    public synchronized T addItem(V item) {
        int selectedIndex = getSelectedIndex();
        super.addItem(item);
        if (selectedIndex != getSelectedIndex()) {
            notifySelectedItemListener();
        }
        return self();
    }

    @Override
    public synchronized V removeItem(int index) {
        int selectedIndex = getSelectedIndex();
        V removeItem = super.removeItem(index);
        if (selectedIndex != getSelectedIndex()) {
            notifySelectedItemListener();
        }
        return removeItem;
    }

    @Override
    public synchronized T clearItems() {
        super.clearItems();
        notifySelectedItemListener();
        return self();
    }

    @Override
    public synchronized T setSelectedIndex(int index) {
        super.setSelectedIndex(index);
        notifySelectedItemListener();
        return self();
    }


    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        int selectedIndex = getSelectedIndex();
        Result result = super.handleKeyStroke(keyStroke);
        if (selectedIndex != getSelectedIndex()) {
            notifySelectedItemListener();
        }
        return result;
    }


    @Override
    protected synchronized void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        int selectedIndex = getSelectedIndex();
        super.afterEnterFocus(direction, previouslyInFocus);
        if (selectedIndex != getSelectedIndex()) {
            notifySelectedItemListener();
        }
    }


    protected void notifySelectedItemListener() {
        itemListeners.forEach(itemListener -> itemListener.onItemSelected(getSelectedIndex(), getSelectedItem()));
    }

}
