package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.SavedApplicationDataService;
import de.chrisgw.sportbooking.service.SportBookingService;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Locale;

import static com.googlecode.lanterna.gui2.Borders.doubleLine;
import static com.googlecode.lanterna.gui2.Direction.HORIZONTAL;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.END;
import static com.googlecode.lanterna.gui2.GridLayout.Alignment.FILL;
import static com.googlecode.lanterna.gui2.GridLayout.createLayoutData;
import static de.chrisgw.sportbooking.SportBookingApplicationTest.*;
import static org.apache.commons.lang3.StringUtils.upperCase;


@Slf4j
public class SportBookingMainWindow extends BasicWindow {

    private final SportBookingService sportBookingService;
    private final SavedApplicationDataService applicationDataService;

    private PendingSportBuchungenPanel pendingSportBuchungenPanel;
    private FinishedSportBuchungenPanel finishedSportBookingPanel;
    private boolean hidePendingSportBuchungenPanel;
    private boolean hideFinishedSportBookingPanel;


    public SportBookingMainWindow(SportBookingService sportBookingService,
            SavedApplicationDataService applicationDataService) {
        super("Buchungsbot - RWTH Hochschulsport");
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS, Hint.NO_POST_RENDERING));
        this.sportBookingService = sportBookingService;
        this.applicationDataService = applicationDataService;

        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.addComponent(createTopMenuBar(), Location.TOP);
        contentPanel.addComponent(createCenterPanel(applicationDataService), Location.CENTER);
        contentPanel.addComponent(createButtonBottomPanel(applicationDataService), Location.BOTTOM);
        setComponent(contentPanel);
    }


    private MenuBar createTopMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.add(createSportBuchungMenu());
        menuBar.add(createPersonenAngabenMenu()).add(createLanguageMenu());
        return menuBar;
    }

    private Menu createSportBuchungMenu() {
        Menu menu = new Menu("SportBuchung");
        menu.add(new MenuItem("new SportBuchung",
                () -> new PersonenAngabenWindow(this.applicationDataService).showDialog(getTextGUI())));
        return menu;
    }

    private Menu createPersonenAngabenMenu() {
        Menu personenAngabenMenu = new Menu("Personenangaben") {

            @Override
            public String getLabel() {
                PersonenAngaben personenAngaben = applicationDataService.getPersonenAngaben();
                return personenAngaben.getName();
            }
        };

        personenAngabenMenu.add(new MenuItem("edit PersonenAngaben", () -> {
            new PersonenAngabenWindow(applicationDataService).showDialog(getTextGUI());
        }));
        return personenAngabenMenu;
    }

    private Menu createLanguageMenu() {
        Menu languageMenu = new Menu("Language / Sprache") {

            @Override
            public String getLabel() {
                return formatLocale(Locale.getDefault());
            }

        };
        languageMenu.add(createLanguageMenuItem(Locale.ENGLISH));
        languageMenu.add(createLanguageMenuItem(Locale.GERMANY));
        return languageMenu;
    }

    private MenuItem createLanguageMenuItem(Locale language) {
        return new MenuItem(formatLocale(language), () -> applicationDataService.setLanguage(language));
    }

    private static String formatLocale(Locale locale) {
        return String.format("%s (%s)", locale.getDisplayLanguage(), upperCase(locale.getLanguage()));
    }


    private Panel createCenterPanel(SavedApplicationDataService savedApplicationDataService) {
        Panel centerPanel = new Panel(new GridLayout(1));
        pendingSportBuchungenPanel = new PendingSportBuchungenPanel(savedApplicationDataService);
        centerPanel.addComponent(pendingSportBuchungenPanel.withBorder(doubleLine("Ausstehende Sportbuchungen")),
                createLayoutData(FILL, FILL, true, true));

        finishedSportBookingPanel = new FinishedSportBuchungenPanel(savedApplicationDataService);
        centerPanel.addComponent(finishedSportBookingPanel.withBorder(doubleLine("Beendete Sportbuchungen")),
                createLayoutData(FILL, END, true, false));
        return centerPanel;
    }

    private Panel createButtonTopPanel(SavedApplicationDataService savedApplicationDataService) {
        Panel buttonTopContainer = new Panel(new LinearLayout(HORIZONTAL));
        buttonTopContainer.addComponent(new Button("Neue Sportbuchung", this::addSportNewBuchungJob));
        buttonTopContainer.addComponent(new Button("Neue Job Bestätigung*", this::addNewSportBuchungsBestaetigung));
        buttonTopContainer.addComponent(new Button("Personenangaben bearbeiten", this::showPersonenAngabenModal));
        buttonTopContainer.addComponent(new Button("clear", savedApplicationDataService::clearAll));
        return buttonTopContainer;
    }

    private Panel createButtonBottomPanel(SavedApplicationDataService savedApplicationDataService) {
        return createButtonTopPanel(savedApplicationDataService);
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
        applicationDataService.addSportBuchungsJob(sportBuchungsJob);
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
        applicationDataService.addFinishedSportBuchung(sportBuchungsBestaetigung);
    }


    private void showPersonenAngabenModal() {
        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(applicationDataService);
        getTextGUI().addWindowAndWait(personenAngabenWindow);
        personenAngabenWindow.getPersonenAngaben().ifPresent(applicationDataService::updatePersonenAngaben);
    }


}
