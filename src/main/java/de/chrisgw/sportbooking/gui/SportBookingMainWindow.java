package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import de.chrisgw.sportbooking.gui.component.FinishedSportBuchungenComponent;
import de.chrisgw.sportbooking.gui.component.MainMenuBar;
import de.chrisgw.sportbooking.gui.component.PendingSportBuchungenComponent;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.SportBookingService;
import de.chrisgw.sportbooking.service.SportBookingSniperService;

import java.util.Arrays;

import static com.googlecode.lanterna.gui2.Borders.singleLineReverseBevel;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;


public class SportBookingMainWindow extends BasicWindow {

    private final SportBookingService bookingService;
    private final SportBookingSniperService bookingSniperService;
    private final ApplicationStateDao applicationStateDao;

    private Panel contentPanel;
    private MainMenuBar mainMenuBar;
    private PendingSportBuchungenComponent pendingComponent;
    private FinishedSportBuchungenComponent finishedComponent;


    public SportBookingMainWindow(SportBookingService bookingService, SportBookingSniperService bookingSniperService,
            ApplicationStateDao applicationStateDao) {
        super("Sportbuchungsbot - RWTH Hochschulsport");
        this.bookingService = bookingService;
        this.bookingSniperService = bookingSniperService;
        this.applicationStateDao = applicationStateDao;
        setHints(Arrays.asList(Hint.EXPANDED));

        contentPanel = new Panel(new BorderLayout());
        setComponent(contentPanel);

        mainMenuBar = new MainMenuBar(applicationStateDao);
        contentPanel.addComponent(mainMenuBar, Location.TOP);

        pendingComponent = new PendingSportBuchungenComponent(applicationStateDao);
        pendingComponent.setLayoutData(GridLayout.createLayoutData(FILL, BEGINNING, true, true));
        finishedComponent = new FinishedSportBuchungenComponent(applicationStateDao);
        finishedComponent.setLayoutData(GridLayout.createLayoutData(FILL, BEGINNING, true, false));

        Panel leftPanel = new Panel(new GridLayout(1));
        leftPanel.addComponent(pendingComponent.withBorder(singleLineReverseBevel("Ausstehende Sportbuchungen")));
        leftPanel.addComponent(finishedComponent.withBorder(singleLineReverseBevel("Beendete Sportbuchungen")));
        contentPanel.addComponent(leftPanel, Location.LEFT);
        addWindowListener(resizeVisibleTableRowListener());

        Panel favoritePanel = new Panel();
        favoritePanel.addComponent(new Label("Meine Favoriten"));
        favoritePanel.addComponent(new CheckBoxList<>().addItem("1 Favorite")
                .addItem("2 Favorite")
                .addItem("3 Favorite")
                .addItem("4 Favorite"));
        contentPanel.addComponent(favoritePanel.withBorder(Borders.singleLine("Favoriten")), Location.CENTER);

        setFocusedInteractable(mainMenuBar.getMenuBar().getMenu(0));
        addWindowListener(windowResizeListener());
    }


    private WindowListenerAdapter windowResizeListener() {
        Label windowSizeDebugLabel = new Label("");
        contentPanel.addComponent(windowSizeDebugLabel, Location.BOTTOM);

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


}
