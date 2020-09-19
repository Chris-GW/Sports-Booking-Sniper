package de.chrisgw.sportsbookingsniper.gui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportsbookingsniper.gui.component.AusstehendeSportBuchungsJobComponent;
import de.chrisgw.sportsbookingsniper.gui.component.FavoriteSportAngebotComponent;
import de.chrisgw.sportsbookingsniper.gui.component.FinishedSportBuchungenComponent;
import de.chrisgw.sportsbookingsniper.gui.component.MainMenuBarComponent;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.Borders.singleLineReverseBevel;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;


public class SportBookingMainWindow extends BasicWindow {

    private final ApplicationStateDao applicationStateDao;

    private MainMenuBarComponent mainMenuBar;
    private AusstehendeSportBuchungsJobComponent pendingComponent;
    private FinishedSportBuchungenComponent finishedComponent;
    private FavoriteSportAngebotComponent favoriteComponent;


    public SportBookingMainWindow(ApplicationStateDao applicationStateDao) {
        super("Sportbuchungsbot - RWTH Hochschulsport");
        this.applicationStateDao = Objects.requireNonNull(applicationStateDao);
        setHints(List.of(Hint.EXPANDED));
        Panel contentPanel = new Panel(new BorderLayout());
        setComponent(contentPanel);

        mainMenuBar = new MainMenuBarComponent(applicationStateDao, this);
        mainMenuBar.focus();

        contentPanel.addComponent(mainMenuBar, Location.TOP);
        contentPanel.addComponent(createLeftPanel(), Location.LEFT);
        contentPanel.addComponent(createCenterPanel(), Location.CENTER);

        Label bottomLabel = new Label("");
        addWindowListener(windowResizeListener(bottomLabel));
        contentPanel.addComponent(bottomLabel, Location.BOTTOM);
        addBasePaneListener(newCloseWindowInputListener());
    }


    private Panel createLeftPanel() {
        pendingComponent = new AusstehendeSportBuchungsJobComponent(applicationStateDao,  this);
        pendingComponent.setLayoutData(GridLayout.createLayoutData(FILL, BEGINNING, true, false));
        mainMenuBar.addViewMenuItemsFor(pendingComponent);
        mainMenuBar.addNavigationMenuItemsFor(pendingComponent);

        finishedComponent = new FinishedSportBuchungenComponent(applicationStateDao, this);
        finishedComponent.setLayoutData(GridLayout.createLayoutData(FILL, BEGINNING, true, false));
        mainMenuBar.addViewMenuItemsFor(finishedComponent);
        mainMenuBar.addNavigationMenuItemsFor(finishedComponent);

        Panel leftPanel = new Panel(new GridLayout(1));
        leftPanel.addComponent(pendingComponent.withBorder(singleLineReverseBevel(pendingComponent.getTitle())));
        leftPanel.addComponent(finishedComponent.withBorder(singleLineReverseBevel(finishedComponent.getTitle())));
        addWindowListener(resizeVisibleTableRowListener());
        return leftPanel;
    }


    private Panel createCenterPanel() {
        favoriteComponent = new FavoriteSportAngebotComponent(applicationStateDao, this);
        mainMenuBar.addViewMenuItemsFor(favoriteComponent);
        mainMenuBar.addNavigationMenuItemsFor(favoriteComponent);

        Panel centerPanel = new Panel();
        centerPanel.addComponent(favoriteComponent.withBorder(singleLineReverseBevel(favoriteComponent.getTitle())));
        return centerPanel;
    }


    private WindowListener windowResizeListener(Label windowSizeDebugLabel) {
        return new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                windowSizeDebugLabel.setText("old = " + oldSize + "; new = " + newSize);
            }

        };
    }


    private WindowListener resizeVisibleTableRowListener() {
        return new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                if (oldSize == null || oldSize.getRows() != newSize.getRows()) {
                    int avabileRows = newSize.getRows() - 2;
                    avabileRows -= mainMenuBar.getPreferredSize().getRows() + 2;
                    avabileRows -= finishedComponent.getPreferredSize().getRows() + 2;
                    int tableRows = avabileRows / 2;
                    pendingComponent.setVisibleRows(Math.max(2, tableRows));
                }
            }

        };
    }


    private BasePaneListener<Window> newCloseWindowInputListener() {
        return new WindowListenerAdapter() {

            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                if (KeyType.Escape.equals(keyStroke.getKeyType()) //
                        || (keyStroke.isAltDown() && KeyType.F4.equals(keyStroke.getKeyType())) //
                        || (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'c')) {
                    MessageDialogButton selectedButton = new MessageDialogBuilder() //
                            .setTitle("SportBookingSniper wirklich beenden?")
                            .setText("Wenn Sie den SportBookingSniper beenden, können keine\n"
                                    + "Buchungsversuche im Hintergrund ausgeführt werden.\n"
                                    + "Wollen Sie den SportBookingSniper wirklich beenden?")
                            .addButton(MessageDialogButton.Abort)
                            .addButton(MessageDialogButton.Yes)
                            .build()
                            .showDialog(getTextGUI());
                    hasBeenHandled.set(true);
                    if (MessageDialogButton.Yes.equals(selectedButton)) {
                        close();
                    }
                }
            }

        };
    }

}
