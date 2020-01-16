package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
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
    private final PendingSportBuchungenWindow pendingSportBuchungenWindow;
    private final FinishedSportBuchungenWindow finishedSportBuchungenWindow;

    private MultiWindowTextGUI windowTextGUI;


    public SportBookingGui(SportBookingService sportBookingService, ApplicationStateDao applicationStateDao) {
        this.sportBookingService = sportBookingService;
        this.applicationStateDao = applicationStateDao;

        backgroundWindow = newBackgroundWindow();

        topMenuBarWindow = new TopMenuBarWindow(applicationStateDao);
        topMenuBarWindow.setHints(Arrays.asList(FIXED_POSITION, NO_POST_RENDERING, NO_DECORATIONS));
        topMenuBarWindow.setPosition(TerminalPosition.TOP_LEFT_CORNER);

        pendingSportBuchungenWindow = new PendingSportBuchungenWindow(applicationStateDao);
        pendingSportBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
        pendingSportBuchungenWindow.setPosition(new TerminalPosition(1, 3));
        pendingSportBuchungenWindow.setSize(new TerminalSize(40, 10));
        pendingSportBuchungenWindow.addWindowListener(positionFinishedBuchungenWindowBelowPending());
        pendingSportBuchungenWindow.addWindowListener(fitWidthForPendingAndFinishedBuchungenWindow());

        finishedSportBuchungenWindow = new FinishedSportBuchungenWindow(applicationStateDao);
        finishedSportBuchungenWindow.setHints(Arrays.asList(FIXED_POSITION, FIXED_SIZE, NO_POST_RENDERING));
        finishedSportBuchungenWindow.setPosition(TerminalPosition.TOP_LEFT_CORNER);
        finishedSportBuchungenWindow.setSize(new TerminalSize(40, 10));
        finishedSportBuchungenWindow.addWindowListener(fitWidthForPendingAndFinishedBuchungenWindow());
    }


    private BasicWindow newBackgroundWindow() {
        BasicWindow backgroundWindow = new BasicWindow() {

            @Override
            public void draw(TextGUIGraphics graphics) {
                super.draw(graphics);

                graphics.applyThemeStyle(getTheme().getDefaultDefinition().getNormal());
                graphics.putString(0, getSize().getRows() - 1, //
                        String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));

                // draw MenuBar till screen end
                graphics.drawLine(0, 0, getSize().getColumns() - 1, 0, ' ');
                graphics.drawLine(0, 1, getSize().getColumns() - 1, 1, Symbols.DOUBLE_LINE_HORIZONTAL);
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
                finishedSportBuchungenWindow.setPosition(pendingSportBuchungenWindow.getPosition()
                        .withRelativeRow(pendingSportBuchungenWindow.getDecoratedSize().getRows())
                        .withRelativeRow(1));
            }

            @Override
            public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
                finishedSportBuchungenWindow.setPosition(pendingSportBuchungenWindow.getPosition()
                        .withRelativeRow(pendingSportBuchungenWindow.getDecoratedSize().getRows())
                        .withRelativeRow(1));
            }

        };
    }


    private WindowListener fitWidthForPendingAndFinishedBuchungenWindow() {
        return new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
                TerminalSize pendingSize = pendingSportBuchungenWindow.getSize();
                TerminalSize finishedBuchungenSize = finishedSportBuchungenWindow.getSize();
                int width = Math.max(pendingSize.getColumns(), finishedBuchungenSize.getColumns());
                if (pendingSize.getColumns() != width) {
                    pendingSportBuchungenWindow.setSize(pendingSize.withColumns(width));
                } else if (finishedBuchungenSize.getColumns() != width) {
                    finishedSportBuchungenWindow.setSize(finishedBuchungenSize.withColumns(width));
                }
            }

        };
    }


    public void showGui(Screen guiScreen) {
        DefaultWindowManager windowManager = new DefaultWindowManager();
        windowTextGUI = new MultiWindowTextGUI(guiScreen, windowManager, new EmptySpace(ANSI.BLUE));

        windowTextGUI.addWindow(topMenuBarWindow);
        windowTextGUI.addWindow(backgroundWindow);
        windowTextGUI.addWindow(pendingSportBuchungenWindow);
        windowTextGUI.addWindow(finishedSportBuchungenWindow);
        windowTextGUI.setActiveWindow(topMenuBarWindow);

        finishedSportBuchungenWindow.setSize(finishedSportBuchungenWindow.getPreferredSize());
        pendingSportBuchungenWindow.setSize(pendingSportBuchungenWindow.getPreferredSize());

        if (applicationStateDao.isFirstVisite()) {
            showFirstVisiteDialog();
        }
        this.windowTextGUI.waitForWindowToClose(topMenuBarWindow);
    }

    private BasicWindow backgroundWindow() {
        return new BasicWindow();
    }


    private void showFirstVisiteDialog() {
        new WelcomeDialog().showDialog(windowTextGUI);

        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(applicationStateDao, true);
        windowTextGUI.addWindowAndWait(personenAngabenWindow);
        PersonenAngaben personenAngaben = personenAngabenWindow.getPersonenAngaben().orElseThrow(RuntimeException::new);
        applicationStateDao.updatePersonenAngaben(personenAngaben);
        applicationStateDao.setFirstVisite(false);
    }


}
