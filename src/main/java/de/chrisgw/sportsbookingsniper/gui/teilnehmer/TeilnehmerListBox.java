package de.chrisgw.sportsbookingsniper.gui.teilnehmer;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.DefaultMutableThemeStyle;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.AbstractListBox;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.component.AbstractWatchableListBox;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.TeilnehmerListeListener;

import java.util.List;


public class TeilnehmerListBox extends AbstractWatchableListBox<Teilnehmer, TeilnehmerListBox>
        implements TeilnehmerListeListener {

    private final ApplicationStateDao applicationStateDao;


    public TeilnehmerListBox(ApplicationStateDao applicationStateDao) {
        this.applicationStateDao = applicationStateDao;
        onChangedTeilnehmerListe(applicationStateDao.getTeilnehmerListe());
    }


    public TeilnehmerListBox deleteSelectedTeilnehmer() {
        applicationStateDao.removeTeilnehmer(getSelectedItem());
        return self();
    }


    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        if (getSelectedIndex() > 0 && KeyType.Delete.equals(keyStroke.getKeyType())) {
            deleteSelectedTeilnehmer();
            return Result.HANDLED;
        } else {
            return super.handleKeyStroke(keyStroke);
        }
    }


    @Override
    public void onChangedTeilnehmerListe(List<Teilnehmer> changedTeilnehmerListe) {
        Teilnehmer selectedTeilnehmer = getSelectedItem();
        clearItems();
        addItem(new Teilnehmer());
        applicationStateDao.getTeilnehmerListe().forEach(this::addItem);
        setSelectedIndex(getItems().indexOf(selectedTeilnehmer));
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addTeilnehmerListeListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeTeilnehmerListeListener(this);
    }


    @Override
    protected ListItemRenderer<Teilnehmer, TeilnehmerListBox> createDefaultListItemRenderer() {
        return new TeilnehmerListItemRenderer();

    }

    public static class TeilnehmerListItemRenderer extends ListItemRenderer<Teilnehmer, TeilnehmerListBox> {

        @Override
        public String getLabel(TeilnehmerListBox listBox, int index, Teilnehmer teilnehmer) {
            if (index == 0) {
                return "Teilnehmer/in hinzufügen";
            } else {
                return teilnehmer.getName();
            }
        }

        @Override
        public void drawItem(TextGUIGraphics graphics, TeilnehmerListBox listBox, int index, Teilnehmer item,
                boolean selected, boolean focused) {
            ThemeDefinition themeDefinition = listBox.getTheme().getDefinition(AbstractListBox.class);
            if (selected && focused) {
                graphics.applyThemeStyle(themeDefinition.getSelected());
            } else if (selected) {
                DefaultMutableThemeStyle themeStyle = new DefaultMutableThemeStyle(themeDefinition.getNormal());
                graphics.applyThemeStyle(themeStyle.setBackground(ANSI.GREEN));
            } else {
                graphics.applyThemeStyle(themeDefinition.getNormal());
            }
            String label = getLabel(listBox, index, item);
            label = TerminalTextUtils.fitString(label, graphics.getSize().getColumns());
            while (TerminalTextUtils.getColumnWidth(label) < graphics.getSize().getColumns()) {
                label += " ";
            }
            graphics.putString(0, 0, label);
        }
    }

}
