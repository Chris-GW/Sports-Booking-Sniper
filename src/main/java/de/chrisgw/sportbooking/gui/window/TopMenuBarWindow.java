package de.chrisgw.sportbooking.gui.window;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.ApplicationStateDao;

import java.util.Locale;

import static de.chrisgw.sportbooking.SportBookingApplicationTest.*;
import static org.apache.commons.lang3.StringUtils.upperCase;


public class TopMenuBarWindow extends BasicWindow {

    private final ApplicationStateDao applicationStateDao;
    private final MenuBar menuBar;


    public TopMenuBarWindow(ApplicationStateDao applicationStateDao) {
        super();
        this.applicationStateDao = applicationStateDao;

        this.menuBar = new MenuBar().setLayoutData(LinearLayout.createLayoutData(Alignment.Fill))
                .add(sportBuchungMenu())
                .add(viewMenu())
                .add(navigationMenu())
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());

        setComponent(Panels.vertical(this.menuBar));
    }


    private Menu sportBuchungMenu() {
        Menu menu = new Menu("SportBuchung");
        menu.add(new MenuItem("new SportBuchung", () -> {
            new PersonenAngabenWindow(this.applicationStateDao).showDialog(getTextGUI());
        }));
        return menu;
    }


    @Override
    public void draw(TextGUIGraphics graphics) {
        super.draw(graphics);

        graphics.putString(new TerminalPosition(0, 1),
                String.format("Pos = %s, pref = %s, size = %s", getPosition(), getPreferredSize(), getSize()));
    }


    private Menu viewMenu() {
        Menu menu = new Menu("View");
        menu.add(new MenuItem("Show / Hide pending SportBuchungen"));
        menu.add(new MenuItem("Show / Hide finished SportBuchungen"));
        return menu;
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

    private static String formatLocale(Locale locale) {
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

}
