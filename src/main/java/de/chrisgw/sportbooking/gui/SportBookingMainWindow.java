package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.gui.component.FavoriteSportAngebotComponent;
import de.chrisgw.sportbooking.gui.component.FinishedSportBuchungenComponent;
import de.chrisgw.sportbooking.gui.component.MainMenuBar;
import de.chrisgw.sportbooking.gui.component.PendingSportBuchungenComponent;
import de.chrisgw.sportbooking.repository.ApplicationStateDao;
import de.chrisgw.sportbooking.repository.SportKatalogRepository;
import de.chrisgw.sportbooking.service.SportBookingSniperService;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.lanterna.gui2.Borders.singleLineReverseBevel;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;


public class SportBookingMainWindow extends BasicWindow implements BasePaneListener<Window> {

    private final SportKatalogRepository sportKatalogRepository;
    private final SportBookingSniperService bookingSniperService;
    private final ApplicationStateDao applicationStateDao;

    private MainMenuBar mainMenuBar;
    private PendingSportBuchungenComponent pendingComponent;
    private FinishedSportBuchungenComponent finishedComponent;
    private FavoriteSportAngebotComponent favoriteComponent;


    public SportBookingMainWindow(SportKatalogRepository sportKatalogRepository,
            SportBookingSniperService bookingSniperService, ApplicationStateDao applicationStateDao) {
        super("Sportbuchungsbot - RWTH Hochschulsport");
        this.sportKatalogRepository = Objects.requireNonNull(sportKatalogRepository);
        this.bookingSniperService = Objects.requireNonNull(bookingSniperService);
        this.applicationStateDao = Objects.requireNonNull(applicationStateDao);
        setHints(Arrays.asList(Hint.EXPANDED));

        mainMenuBar = new MainMenuBar(sportKatalogRepository, applicationStateDao, this);
        setFocusedInteractable(mainMenuBar.getMenuBar().getMenu(0));

        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.addComponent(mainMenuBar, Location.TOP);
        contentPanel.addComponent(createLeftPanel(), Location.LEFT);
        contentPanel.addComponent(createCenterPanel(), Location.CENTER);

        Label bottomLabel = new Label("");
        addWindowListener(windowResizeListener(bottomLabel));
        contentPanel.addComponent(bottomLabel, Location.BOTTOM);
        setComponent(contentPanel);
        addBasePaneListener(this);
    }


    private Panel createLeftPanel() {
        pendingComponent = new PendingSportBuchungenComponent(applicationStateDao, this);
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
                    pendingComponent.getPendingJobsTabel().setVisibleRows(Math.max(2, tableRows));
                }
            }

        };
    }


    @Override
    public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
        // noop
    }

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
            if (MessageDialogButton.Yes.equals(selectedButton)) {
                close();
            }
            hasBeenHandled.set(true);
        }
    }

}
