package de.chrisgw.sportbooking.gui.window;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.gui.component.CheckBoxMenuItem;
import de.chrisgw.sportbooking.gui.component.MultipleWindowNavigator;
import de.chrisgw.sportbooking.gui.component.SportBuchungsBotLogo;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.ApplicationStateDao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

import static com.googlecode.lanterna.gui2.GridLayout.Alignment.BEGINNING;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;
import static de.chrisgw.sportbooking.SportBookingApplicationTest.*;
import static org.apache.commons.lang3.StringUtils.upperCase;


public class TopMenuBarWindow extends BasicWindow {

    private final ApplicationStateDao applicationStateDao;

    private MenuBar menuBar;
    private Menu viewMenu;


    public TopMenuBarWindow(ApplicationStateDao applicationStateDao) {
        super("top MenuBar");
        this.applicationStateDao = applicationStateDao;
        setHints(Arrays.asList(Hint.FIXED_POSITION, Hint.FULL_SCREEN, Hint.NO_DECORATIONS));
        setPosition(TerminalPosition.TOP_LEFT_CORNER);

        Panel contentPanel = new Panel(createFullScreenGridLayout(1));
        contentPanel.addComponent(createTopMenuBar(), GridLayout.createHorizontallyFilledLayoutData(1));
        contentPanel.addComponent(new EmptySpace(ANSI.BLUE), GridLayout.createHorizontallyFilledLayoutData(1));
        contentPanel.addComponent(createLogoPanel(), GridLayout.createHorizontallyFilledLayoutData(1));
        contentPanel.addComponent(new EmptySpace(ANSI.BLUE), GridLayout.createLayoutData(FILL, FILL, true, true, 1, 1));
        setComponent(contentPanel);
    }


    private Panel createTopMenuBar() {
        this.viewMenu = new Menu("View");
        this.menuBar = new MenuBar().add(sportBuchungMenu())
                .add(viewMenu)
                .add(navigationMenu())
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());

        Panel topMenuBar = new Panel(new BorderLayout());
        topMenuBar.addComponent(menuBar, Location.CENTER);
        topMenuBar.addComponent(clockDisplyMenuBar(), Location.RIGHT);
        return topMenuBar;
    }


    private Panel createLogoPanel() {
        TextColor backgroundColor = ANSI.BLUE;
        SportBuchungsBotLogo sportBuchungsBotLogo = new SportBuchungsBotLogo(ANSI.YELLOW, backgroundColor);

        Panel logoPanel = new Panel(createFullScreenGridLayout(3));
        logoPanel.addComponent(new EmptySpace(backgroundColor), GridLayout.createLayoutData(FILL, FILL));
        logoPanel.addComponent(sportBuchungsBotLogo, GridLayout.createLayoutData(BEGINNING, BEGINNING));
        logoPanel.addComponent(new EmptySpace(backgroundColor), GridLayout.createLayoutData(FILL, FILL, true, true));
        return logoPanel;
    }


    private Menu sportBuchungMenu() {
        Menu menu = new Menu("New");
        menu.add(new MenuItem("new SportBuchung", () -> {
            // TODO new SportBuchung dialog
        }));
        return menu;
    }


    public CheckBoxMenuItem addToggleVisibleWindowMenuItem(MultipleWindowNavigator windowNavigator, Window window) {
        String label = window.getTitle();
        KeyType windowShortKeyType = windowNavigator.findShortKeyTypeForWindow(window).orElse(null);
        if (windowShortKeyType != null) {
            label += " <S-" + windowShortKeyType + ">";
        }
        CheckBoxMenuItem checkBoxMenuItem = new CheckBoxMenuItem(label,
                visibleCheck -> windowNavigator.setWindowVisible(window, visibleCheck));
        checkBoxMenuItem.setChecked(window.isVisible());
        viewMenu.add(checkBoxMenuItem);
        return checkBoxMenuItem;
    }


    private Menu navigationMenu() {
        Menu menu = new Menu("Navigation");
        menu.add(new MenuItem("next Window", () -> getTextGUI().cycleActiveWindow(false)));
        menu.add(new MenuItem("previous Window", () -> getTextGUI().cycleActiveWindow(true)));
        menu.add(new MenuItem("pending SportBuchungen"));
        menu.add(new MenuItem("finished SportBuchungen"));
        menu.add(new MenuItem("focuse Navbar [Ctrl + " + Symbols.ARROW_UP + "]"));
        return menu;
    }


    private Menu personenAngabenMenu() {
        Menu menu = new Menu("Personenangaben") {

            @Override
            public String getLabel() {
                PersonenAngaben personenAngaben = applicationStateDao.getPersonenAngaben();
                return personenAngaben.getName();
            }
        };

        menu.add(new MenuItem("edit PersonenAngaben", () -> {
            new PersonenAngabenWindow(applicationStateDao).showDialog(getTextGUI());
        }));
        return menu;
    }


    private Menu languageMenu() {
        Menu menu = new Menu("Language / Sprache") {

            @Override
            public String getLabel() {
                return formatLocale(Locale.getDefault());
            }

        };
        menu.add(createLanguageMenuItem(Locale.ENGLISH));
        menu.add(createLanguageMenuItem(Locale.GERMANY));
        return menu;
    }

    private MenuItem createLanguageMenuItem(Locale language) {
        return new MenuItem(formatLocale(language), () -> applicationStateDao.setLanguage(language));
    }

    private String formatLocale(Locale locale) {
        return String.format("%s (%s)", locale.getDisplayLanguage(), upperCase(locale.getLanguage()));
    }


    private Menu debugMenu() {
        Menu menu = new Menu("Debug");
        menu.add(new MenuItem("addSportNewBuchungJob", this::addSportNewBuchungJob));
        menu.add(new MenuItem("addNewSportBuchungsBestaetigung", this::addNewSportBuchungsBestaetigung));
        return menu;
    }

    private void addSportNewBuchungJob() {
        SportArt sportArt = new SportArt("Badminton Level Spielbetrieb", "http://badminton-spielbetrieb.de");
        SportAngebot sportAngebot = createMontagsSportAngebot(sportArt);
        SportTermin sportTermin = sportAngebot.getSportTermine()
                .stream()
                .unordered()
                .findAny()
                .orElseThrow(RuntimeException::new);
        SportBuchungsJob sportBuchungsJob = new SportBuchungsJob(sportTermin, createPersonenAngaben());
        applicationStateDao.addSportBuchungsJob(sportBuchungsJob);
    }

    private void addNewSportBuchungsBestaetigung() {
        SportArt sportArt = new SportArt("Badminton Level 2", "http://badminton-level-2.de");
        SportAngebot sportAngebot = createFreitagsSportAngebot(sportArt);
        SportTermin sportTermin = sportAngebot.getSportTermine()
                .stream()
                .unordered()
                .findAny()
                .orElseThrow(RuntimeException::new);
        SportBuchungsBestaetigung sportBuchungsBestaetigung = createSportBuchungsBestaetigung(sportTermin);
        applicationStateDao.addFinishedSportBuchung(sportBuchungsBestaetigung);
    }


    private MenuBar clockDisplyMenuBar() {
        Menu clockDisplyMenu = new Menu("clock") {

            private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

            @Override
            public String getLabel() {
                LocalDateTime time = LocalDateTime.now().withNano(0);
                return dateTimeFormatter.format(time);
            }

            @Override
            public boolean isInvalid() {
                return true;
            }

        };
        clockDisplyMenu.setTheme(new SimpleTheme(ANSI.BLACK, ANSI.WHITE, SGR.BOLD));
        clockDisplyMenu.setEnabled(false);

        MenuBar menuBar = new MenuBar() {

            private LocalDateTime lastShownTime = LocalDateTime.now().minusSeconds(1);

            @Override
            public boolean isInvalid() {
                LocalDateTime currentTime = LocalDateTime.now().withNano(0);
                boolean isInvalid = lastShownTime.isBefore(currentTime);
                lastShownTime = currentTime;
                return isInvalid;
            }
        };
        return menuBar.add(clockDisplyMenu);
    }


    @Override
    public void draw(TextGUIGraphics graphics) {
        super.draw(graphics);

        graphics.putString(new TerminalPosition(0, 1),
                String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));
    }


    private static GridLayout createFullScreenGridLayout(int numberOfColums) {
        return new GridLayout(numberOfColums).setHorizontalSpacing(0).setRightMarginSize(0).setLeftMarginSize(0);
    }

}
