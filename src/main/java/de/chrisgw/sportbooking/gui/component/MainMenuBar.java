package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.ApplicationStateDao;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static de.chrisgw.sportbooking.SportBookingApplicationTest.*;
import static org.apache.commons.lang3.StringUtils.upperCase;


public class MainMenuBar extends SportBookingComponent {

    @Getter
    private MenuBar menuBar;

    @Getter
    private Menu viewMenu;

    @Getter
    private Menu navigationMenu;


    public MainMenuBar(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "top Navigation", KeyType.F1);
        setLayoutManager(new BorderLayout());

        this.viewMenu = new Menu("View");
        this.navigationMenu = new Menu("Navigation");
        this.menuBar = new MenuBar().add(sportBuchungMenu())
                .add(viewMenu)
                .add(navigationMenu)
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());
        addViewMenuItemsFor(this);
        addNavigationMenuItemsFor(this);

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


    public void addViewMenuItemsFor(SportBookingComponent sportBookingComponent) {
        KeyType shortKey = sportBookingComponent.getShortKeyType();
        String label = sportBookingComponent.getTitle();
        if (shortKey != null) {
            label += " <S-" + shortKey + ">";
        }
        CheckBoxMenuItem checkBoxMenuItem = new CheckBoxMenuItem(label, sportBookingComponent::setVisible);
        checkBoxMenuItem.setChecked(sportBookingComponent.isVisible());
        viewMenu.add(checkBoxMenuItem);
    }

    public void addNavigationMenuItemsFor(SportBookingComponent sportBookingComponent) {
        KeyType shortKey = sportBookingComponent.getShortKeyType();
        String label = sportBookingComponent.getTitle();
        if (shortKey != null) {
            label += " <" + shortKey + ">";
        }
        MenuItem navigationMenuItem = new MenuItem(label, () -> {
            window.setFocusedInteractable(sportBookingComponent.nextFocus(null));
        });
        navigationMenu.add(navigationMenuItem);
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

}
