package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static de.chrisgw.sportbooking.SportBookingApplicationTest.*;
import static org.apache.commons.lang3.StringUtils.upperCase;


public class MainMenuBar extends Panel {

    private final ApplicationStateDao applicationStateDao;

    private MenuBar menuBar;
    private Menu viewMenu;
    private Menu navigationMenu;


    public MainMenuBar(ApplicationStateDao applicationStateDao) {
        super(new BorderLayout());
        this.applicationStateDao = applicationStateDao;

        this.viewMenu = new Menu("View");
        this.navigationMenu = new Menu("Navigation");
        this.menuBar = new MenuBar().add(sportBuchungMenu())
                .add(viewMenu)
                .add(navigationMenu)
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());

        addComponent(menuBar, Location.CENTER);
        addComponent(clockDisplyMenuBar(), Location.RIGHT);
    }


    private Menu sportBuchungMenu() {
        Menu menu = new Menu("New");
        menu.add(new MenuItem("new SportBuchung", () -> {
            // TODO new SportBuchung dialog
        }));
        return menu;
    }


    public void fillViewMenu(List<Pair<KeyType, Window>> shortKeyTypeWindowPairs) {
        for (Pair<KeyType, Window> shortKeyTypeWindowPair : shortKeyTypeWindowPairs) {
            KeyType shortKey = shortKeyTypeWindowPair.getLeft();
            Window window = shortKeyTypeWindowPair.getRight();
            String label = window.getTitle();
            if (shortKey != null) {
                label += " <S-" + shortKey + ">";
            }
            CheckBoxMenuItem checkBoxMenuItem = new CheckBoxMenuItem(label, window::setVisible);
            checkBoxMenuItem.setChecked(window.isVisible());
            viewMenu.add(checkBoxMenuItem);
        }
    }

    public void fillNavigationMenu(List<Pair<KeyType, Window>> shortKeyTypeWindowPairs) {
        for (Pair<KeyType, Window> shortKeyTypeWindowPair : shortKeyTypeWindowPairs) {
            KeyType shortKey = shortKeyTypeWindowPair.getLeft();
            Window window = shortKeyTypeWindowPair.getRight();
            String label = window.getTitle();
            if (shortKey != null) {
                label += " <" + shortKey + ">";
            }
            navigationMenu.add(new MenuItem(label, () -> getTextGUI().setActiveWindow(window)));
        }
        navigationMenu.add(new MenuItem("focus Window above <C-" + Symbols.ARROW_UP + ">"));
        navigationMenu.add(new MenuItem("focus Window below <C-" + Symbols.ARROW_DOWN + ">"));
        navigationMenu.add(new MenuItem("focus left  Window <C-" + Symbols.ARROW_LEFT + ">"));
        navigationMenu.add(new MenuItem("focus right Window <C-" + Symbols.ARROW_RIGHT + ">"));
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
            new PersonenAngabenDialog(applicationStateDao).showDialog(getTextGUI());
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
    protected void onAfterDrawing(TextGUIGraphics graphics) {
//        graphics.putString(new TerminalPosition(0, 1),
//                String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));
    }


    @Override
    public WindowBasedTextGUI getTextGUI() {
        return (WindowBasedTextGUI) super.getTextGUI();
    }


    public MenuBar getMenuBar() {
        return menuBar;
    }

}
