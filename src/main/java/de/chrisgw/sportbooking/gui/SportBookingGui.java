package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import de.chrisgw.sportbooking.gui.component.MultipleWindowNavigator;
import de.chrisgw.sportbooking.gui.window.*;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import de.chrisgw.sportbooking.service.SportBookingService;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static com.googlecode.lanterna.gui2.Window.Hint.*;


@Slf4j
public class SportBookingGui extends WindowListenerAdapter {

    private final SportBookingService sportBookingService;
    private final ApplicationStateDao applicationStateDao;

    private final Window backgroundWindow;
    private final TopMenuBarWindow topMenuBarWindow;
    private final PendingSportBuchungenWindow pendingBuchungenWindow;
    private final FinishedSportBuchungenWindow finishedBuchungenWindow;

    private MultipleWindowNavigator windowNavigator;


    public SportBookingGui(SportBookingService sportBookingService, ApplicationStateDao applicationStateDao) {
        this.sportBookingService = sportBookingService;
        this.applicationStateDao = applicationStateDao;

        backgroundWindow = newBackgroundWindow();

        topMenuBarWindow = new TopMenuBarWindow(applicationStateDao);
        topMenuBarWindow.setHints(Arrays.asList(FIXED_POSITION, NO_POST_RENDERING, NO_DECORATIONS));
        topMenuBarWindow.setPosition(TerminalPosition.TOP_LEFT_CORNER);

        pendingBuchungenWindow = new PendingSportBuchungenWindow(applicationStateDao);
        pendingBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
        pendingBuchungenWindow.setPosition(new TerminalPosition(1, 3));
        pendingBuchungenWindow.setSize(new TerminalSize(40, 10));
        pendingBuchungenWindow.addWindowListener(positionFinishedBuchungenWindowBelowPending());
        pendingBuchungenWindow.addWindowListener(fitWidthForPendingAndFinishedBuchungenWindow());

        finishedBuchungenWindow = new FinishedSportBuchungenWindow(applicationStateDao);
        finishedBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
        finishedBuchungenWindow.setPosition(TerminalPosition.TOP_LEFT_CORNER);
        finishedBuchungenWindow.setSize(new TerminalSize(40, 10));
        finishedBuchungenWindow.addWindowListener(fitWidthForPendingAndFinishedBuchungenWindow());
    }


    private BasicWindow newBackgroundWindow() {
        BasicWindow backgroundWindow = new BasicWindow() {

            @Override
            public void draw(TextGUIGraphics graphics) {
                super.draw(graphics);

                graphics.applyThemeStyle(getTheme().getDefaultDefinition().getNormal());
                graphics.putString(0, getSize().getRows() - 1, //
                        String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));

                // draw topMenuBar till screen end
                if (topMenuBarWindow.isVisible()) {
                    graphics.drawLine(0, 0, getSize().getColumns() - 1, 0, ' ');
                    graphics.drawLine(0, 1, getSize().getColumns() - 1, 1, Symbols.DOUBLE_LINE_HORIZONTAL);
                }
            }
        };
        backgroundWindow.setHints(Arrays.asList(FULL_SCREEN, NO_FOCUS, NO_POST_RENDERING, NO_DECORATIONS));
        backgroundWindow.setComponent(new EmptySpace(ANSI.BLUE));
        return backgroundWindow;
    }


    private WindowListener positionFinishedBuchungenWindowBelowPending() {
        return new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                finishedBuchungenWindow.setPosition(pendingBuchungenWindow.getPosition()
                        .withRelativeRow(pendingBuchungenWindow.getDecoratedSize().getRows())
                        .withRelativeRow(1));
            }

            @Override
            public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
                finishedBuchungenWindow.setPosition(pendingBuchungenWindow.getPosition()
                        .withRelativeRow(pendingBuchungenWindow.getDecoratedSize().getRows())
                        .withRelativeRow(1));
            }

        };
    }


    private WindowListener fitWidthForPendingAndFinishedBuchungenWindow() {
        return new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                TerminalSize pendingSize = pendingBuchungenWindow.getSize();
                TerminalSize finishedBuchungenSize = finishedBuchungenWindow.getSize();
                int width = Math.max(pendingSize.getColumns(), finishedBuchungenSize.getColumns());
                if (pendingSize.getColumns() != width) {
                    pendingBuchungenWindow.setSize(pendingSize.withColumns(width));
                } else if (finishedBuchungenSize.getColumns() != width) {
                    finishedBuchungenWindow.setSize(finishedBuchungenSize.withColumns(width));
                }
            }

        };
    }


    public void showGui(Screen guiScreen) {
        DefaultWindowManager windowManager = new DefaultWindowManager();
        MultiWindowTextGUI windowTextGUI = new MultiWindowTextGUI(guiScreen, windowManager, new EmptySpace(ANSI.BLUE));
        windowTextGUI.addWindow(backgroundWindow);

        windowNavigator = new MultipleWindowNavigator(windowTextGUI);
        windowNavigator.addManagedWindow(topMenuBarWindow, new KeyStroke(KeyType.F2));
        windowNavigator.addManagedWindow(pendingBuchungenWindow, new KeyStroke(KeyType.F3));
        windowNavigator.addManagedWindow(finishedBuchungenWindow, new KeyStroke(KeyType.F4));
        windowNavigator.connectWindowsVertically(topMenuBarWindow, pendingBuchungenWindow);
        windowNavigator.connectWindowsVertically(pendingBuchungenWindow, finishedBuchungenWindow);
        windowNavigator.setActiveWindow(topMenuBarWindow);

        finishedBuchungenWindow.setSize(finishedBuchungenWindow.getPreferredSize());
        pendingBuchungenWindow.setSize(pendingBuchungenWindow.getPreferredSize());

        if (applicationStateDao.isFirstVisite()) {
            showFirstVisiteDialog();
        }
        windowTextGUI.waitForWindowToClose(topMenuBarWindow);
    }


    private void showFirstVisiteDialog() {
        new WelcomeDialog().showDialog(windowNavigator.getWindowTextGUI());

        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(applicationStateDao, true);
        windowNavigator.getWindowTextGUI().addWindowAndWait(personenAngabenWindow);
        PersonenAngaben personenAngaben = personenAngabenWindow.getPersonenAngaben().orElseThrow(RuntimeException::new);
        applicationStateDao.updatePersonenAngaben(personenAngaben);
        applicationStateDao.setFirstVisite(false);
    }


}
