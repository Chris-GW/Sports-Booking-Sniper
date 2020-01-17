package de.chrisgw.sportbooking.gui;

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
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

import static com.googlecode.lanterna.gui2.Window.Hint.*;


@Slf4j
public class SportBookingGui extends WindowListenerAdapter {

    private final SportBookingService sportBookingService;
    private final ApplicationStateDao applicationStateDao;

    private final TopMenuBarWindow topMenuBarWindow;
    private final PendingSportBuchungenWindow pendingBuchungenWindow;
    private final FinishedSportBuchungenWindow finishedBuchungenWindow;

    private MultiWindowTextGUI multiWindowTextGUI;
    private MultipleWindowNavigator windowNavigator;


    public SportBookingGui(SportBookingService sportBookingService, ApplicationStateDao applicationStateDao) {
        this.sportBookingService = sportBookingService;
        this.applicationStateDao = applicationStateDao;

        windowNavigator = new MultipleWindowNavigator(this);
        topMenuBarWindow = createTopMenuBarWindow(applicationStateDao);
        pendingBuchungenWindow = createPendingSportBuchungenWindow(applicationStateDao);
        finishedBuchungenWindow = createFinishedBuchungenWindow(applicationStateDao);
        fitSameWidth(pendingBuchungenWindow, finishedBuchungenWindow);

        windowNavigator.connectWindowsVertically(topMenuBarWindow, pendingBuchungenWindow);
        windowNavigator.connectWindowsVertically(pendingBuchungenWindow, finishedBuchungenWindow);
        windowNavigator.getWindowShortKeyTypeMapping().stream().map(Pair::getRight).forEachOrdered(window -> {
            topMenuBarWindow.addToggleVisibleWindowMenuItem(windowNavigator, window);
        });
    }


    private PendingSportBuchungenWindow createPendingSportBuchungenWindow(ApplicationStateDao applicationStateDao) {
        PendingSportBuchungenWindow pendingBuchungenWindow = new PendingSportBuchungenWindow(applicationStateDao);
        pendingBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
        pendingBuchungenWindow.setPosition(new TerminalPosition(1, 3));
        pendingBuchungenWindow.setSize(new TerminalSize(40, 10));
//        pendingBuchungenWindow.setStrictFocusChange(true);
        pendingBuchungenWindow.addWindowListener(positionFinishedBuchungenWindowBelowPending());
        windowNavigator.addManagedWindow(pendingBuchungenWindow, new KeyStroke(KeyType.F3));
        return pendingBuchungenWindow;
    }

    private FinishedSportBuchungenWindow createFinishedBuchungenWindow(ApplicationStateDao applicationStateDao) {
        FinishedSportBuchungenWindow finishedBuchungenWindow = new FinishedSportBuchungenWindow(applicationStateDao);
        finishedBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
//        finishedBuchungenWindow.setStrictFocusChange(true);
        finishedBuchungenWindow.setPosition(TerminalPosition.TOP_LEFT_CORNER);
        finishedBuchungenWindow.setSize(new TerminalSize(40, 10));

        windowNavigator.addManagedWindow(finishedBuchungenWindow, new KeyStroke(KeyType.F4));
        return finishedBuchungenWindow;
    }

    private TopMenuBarWindow createTopMenuBarWindow(ApplicationStateDao applicationStateDao) {
        TopMenuBarWindow topMenuBarWindow = new TopMenuBarWindow(applicationStateDao);
//        topMenuBarWindow.setStrictFocusChange(true);
        windowNavigator.addManagedWindow(topMenuBarWindow, new KeyStroke(KeyType.F2));
        return topMenuBarWindow;
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


    private void fitSameWidth(Window firstWindow, Window secondWindow) {
        WindowListenerAdapter windowResizeListener = new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                int firstPreferredSize = firstWindow.getPreferredSize().getColumns();
                int secondPreferredSize = secondWindow.getPreferredSize().getColumns();
                int fittedWidth = Math.max(firstPreferredSize, secondPreferredSize);

                if (firstPreferredSize != fittedWidth && !window.equals(firstWindow)) {
                    firstWindow.setSize(firstWindow.getSize().withColumns(fittedWidth));
                }
                if (secondPreferredSize != fittedWidth && !window.equals(secondWindow)) {
                    secondWindow.setSize(secondWindow.getSize().withColumns(fittedWidth));
                }
            }

        };

        firstWindow.addWindowListener(windowResizeListener);
        secondWindow.addWindowListener(windowResizeListener);
    }


    public void showGui(Screen guiScreen) {
        DefaultWindowManager windowManager = new DefaultWindowManager();
        MultiWindowTextGUI windowTextGUI = new MultiWindowTextGUI(guiScreen, windowManager, new EmptySpace(ANSI.BLUE)) {

            @Override
            public synchronized WindowBasedTextGUI moveToTop(Window window) {
                if (window instanceof TopMenuBarWindow) {
                    return this;
                }
                return super.moveToTop(window);
            }
        };
        this.multiWindowTextGUI = windowTextGUI;

        windowTextGUI.addWindow(topMenuBarWindow);
        windowTextGUI.addWindow(pendingBuchungenWindow);
        windowTextGUI.addWindow(finishedBuchungenWindow);
        windowNavigator.setActiveWindow(topMenuBarWindow);

        finishedBuchungenWindow.setSize(finishedBuchungenWindow.getPreferredSize());
        pendingBuchungenWindow.setSize(pendingBuchungenWindow.getPreferredSize());

        if (applicationStateDao.isFirstVisite()) {
            showFirstVisiteDialog();
        }
        windowTextGUI.waitForWindowToClose(topMenuBarWindow);
    }


    private void showFirstVisiteDialog() {
        new WelcomeDialog().showDialog(getTextGUI());

        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(applicationStateDao, true);
        getTextGUI().addWindowAndWait(personenAngabenWindow);
        PersonenAngaben personenAngaben = personenAngabenWindow.getPersonenAngaben().orElseThrow(RuntimeException::new);
        applicationStateDao.updatePersonenAngaben(personenAngaben);
        applicationStateDao.setFirstVisite(false);
    }


    public WindowBasedTextGUI getTextGUI() {
        return multiWindowTextGUI;
    }

}
