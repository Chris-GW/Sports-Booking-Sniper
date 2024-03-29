package de.chrisgw.sportsbookingsniper.gui.menu;

import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.buchung.SportBuchungDialog;
import de.chrisgw.sportsbookingsniper.gui.component.AnimatedClock;
import de.chrisgw.sportsbookingsniper.gui.component.BasicPanelComponent;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.teilnehmer.TeilnehmerFormDialog;
import de.chrisgw.sportsbookingsniper.gui.teilnehmer.TeilnehmerVerwaltungWindow;

import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.upperCase;


public class MainMenuBarComponent extends BasicPanelComponent {

    private final MenuBar menuBar = new MenuBar();
    private final Menu viewMenu = new Menu("View");
    private final Menu navigationMenu = new Menu("Navigation");


    public MainMenuBarComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "top Navigation", KeyType.F1);
        setLayoutManager(new BorderLayout());

        viewMenu.add(new MenuItem("Exit", () -> getTextGUI().getActiveWindow().close()));
        viewMenu.add(createSwitchThemeMenuItem());
        addViewMenuItemsFor(this);

        addNavigationMenuItemsFor(this);

        menuBar.add(sportBuchungMenu())
                .add(viewMenu)
                .add(navigationMenu)
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());

        addComponent(menuBar, Location.CENTER);
        addComponent(new AnimatedClock(), Location.RIGHT);
    }

    private MenuItem createSwitchThemeMenuItem() {
        return new MenuItem("Switch Theme", () -> {
            String[] registerdThemes = LanternaThemes.getRegisteredThemes().toArray(new String[0]);
            ListSelectDialog<String> selectThemeDialog = new ListSelectDialogBuilder<String>().setTitle("Switch Theme")
                    .setDescription(null)
                    .setCanCancel(true)
                    .addListItems(registerdThemes)
                    .build();

            String selectedThemeName = selectThemeDialog.showDialog(getTextGUI());
            if (selectedThemeName != null) {
                Theme selectedTheme = LanternaThemes.getRegisteredTheme(selectedThemeName);
                getTextGUI().setTheme(selectedTheme);
                applicationStateDao.setSelectedTheme(selectedThemeName);
            }
        });
    }


    private Menu sportBuchungMenu() {
        Menu menu = new Menu("New");
        menu.add(new MenuItem("new SportBuchung", () -> {
            SportBuchungDialog sportBuchungDialog = new SportBuchungDialog(applicationStateDao);
            Optional<SportBuchungsJob> sportBuchungsJob = sportBuchungDialog.showDialog(getTextGUI());
            sportBuchungsJob.ifPresent(applicationStateDao::addSportBuchungsJob);
        }));
        return menu;
    }


    public void addViewMenuItemsFor(BasicPanelComponent basicPanelComponent) {
        KeyType shortKey = basicPanelComponent.getShortKeyType();
        String label = basicPanelComponent.getTitle();
        if (shortKey != null) {
            label += " <S-" + shortKey + ">";
        }
        CheckBoxMenuItem checkBoxMenuItem = new CheckBoxMenuItem(label, basicPanelComponent::setVisible);
        checkBoxMenuItem.setChecked(basicPanelComponent.isVisible());
        viewMenu.add(checkBoxMenuItem);
    }

    public void addNavigationMenuItemsFor(BasicPanelComponent basicPanelComponent) {
        KeyType shortKey = basicPanelComponent.getShortKeyType();
        String label = basicPanelComponent.getTitle();
        if (shortKey != null) {
            label += " <" + shortKey + ">";
        }
        MenuItem navigationMenuItem = new MenuItem(label, () -> {
            window.setFocusedInteractable(basicPanelComponent.nextFocus(null));
        });
        navigationMenu.add(navigationMenuItem);
    }


    private Menu personenAngabenMenu() {
        Menu menu = new Menu("Teilnehmer") {

            @Override
            public String getLabel() {
                Teilnehmer teilnehmer = applicationStateDao.getDefaultTeilnehmer();
                return teilnehmer != null ? teilnehmer.getName() : "Teilnehmer";
            }
        };

        menu.add(new MenuItem("Teilnehmer Dialog",
                () -> getTextGUI().addWindow(new TeilnehmerVerwaltungWindow(applicationStateDao))));
        menu.add(new MenuItem("add einen neuen Teilnehmer", this::showAddNewTeilnehmerDialog));
        menu.add(new MenuItem("bearbeite einen Teilnehmer", this::showEditTeilnehmerActionDialog));
        menu.add(new MenuItem("lösche einen Teilnehmer", this::showDeleteTeilnehmerActionDialog));
        menu.add(new MenuItem("switch default Teilnehmer", this::showSwitchDefaultTeilnehmerActionDialog));
        return menu;
    }

    private void showAddNewTeilnehmerDialog() {
        Optional<Teilnehmer> savedTeilnehmer = new TeilnehmerFormDialog().showDialog(getTextGUI());
        savedTeilnehmer.ifPresent(applicationStateDao::addTeilnehmer);
    }

    private void showSwitchDefaultTeilnehmerActionDialog() {
        var actionDialogBuilder = new ActionListDialogBuilder();
        actionDialogBuilder.setTitle("Switch default Teilnehmer to ...");
        actionDialogBuilder.setCloseAutomaticallyOnAction(true);
        actionDialogBuilder.setCanCancel(true);
        applicationStateDao.getTeilnehmerListe().forEach(teilnehmer -> {
            String label = teilnehmer.getName();
            actionDialogBuilder.addAction(label, () -> {
                applicationStateDao.setDefaultTeilnehmer(teilnehmer);
            });
        });
        actionDialogBuilder.build().showDialog(getTextGUI());
    }

    private void showEditTeilnehmerActionDialog() {
        var actionDialogBuilder = new ActionListDialogBuilder();
        actionDialogBuilder.setTitle("Teilnehmer zum Bearbeiten auswählen");
        actionDialogBuilder.setCloseAutomaticallyOnAction(true);
        actionDialogBuilder.setCanCancel(true);
        applicationStateDao.getTeilnehmerListe().forEach(teilnehmer -> {
            String label = teilnehmer.getName();
            actionDialogBuilder.addAction(label, () -> {
                Optional<Teilnehmer> savedTeilnehmer = new TeilnehmerFormDialog(teilnehmer).showDialog(getTextGUI());
                if (savedTeilnehmer.isPresent()) {
                    teilnehmer.set(savedTeilnehmer.get());
                    int index = applicationStateDao.getTeilnehmerListe().indexOf(teilnehmer);
                    applicationStateDao.updateTeilnehmer(index, savedTeilnehmer.get());
                }
            });
        });
        actionDialogBuilder.build().showDialog(getTextGUI());
    }

    private void showDeleteTeilnehmerActionDialog() {
        var actionDialogBuilder = new ActionListDialogBuilder();
        actionDialogBuilder.setTitle("Select Teilnehmer for deletion");
        actionDialogBuilder.setCloseAutomaticallyOnAction(true);
        actionDialogBuilder.setCanCancel(true);
        applicationStateDao.getTeilnehmerListe().forEach(teilnehmer -> {
            String label = teilnehmer.getName();
            actionDialogBuilder.addAction(label, () -> {
                applicationStateDao.removeTeilnehmer(teilnehmer);
            });
        });
        actionDialogBuilder.build().showDialog(getTextGUI());
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
        Menu debugMenu = new Menu("Debug");
        debugMenu.add(new MenuItem("new dummy pending SportBuchungsJob", this::addPendingDummySportBookingJob));
        debugMenu.add(new MenuItem("new dummy finished SportBuchungsJob", this::addFinishDummySportBookingJob));
        debugMenu.add(new MenuItem("show FileDialog", () -> {
            new FileDialogBuilder().setTitle("title")
                    .setActionLabel("action label")
                    .setDescription("description")
                    .build()
                    .showDialog(getTextGUI());
        }));
        return debugMenu;
    }

    private void addPendingDummySportBookingJob() {
        applicationStateDao.addSportBuchungsJob(SportBookingModelTestUtil.newSportBuchungsJob());

//        SportKatalog sportKatalog = applicationStateDao.currentSportKatalog();
//        String sportArtName = "Fechten Level 2 Techniktraining";
//        String kursnummer = "33422246";
//        SportArt sportArt = sportKatalog.findSportArtByName(sportArtName).orElseThrow(RuntimeException::new);
//        SportAngebot sportAngebot = sportArt.findSportAngebot(kursnummer).orElseThrow(RuntimeException::new);
//        SportTermin sportTermin = sportAngebot.bevorstehendeSportTermine()
//                .findFirst()
//                .orElseThrow(RuntimeException::new);
//
//        SportBuchungsJob buchungsJob = new SportBuchungsJob();
//        buchungsJob.setJobId(5);
//        buchungsJob.setTeilnehmerListe(applicationStateDao.getTeilnehmerListe());
//        buchungsJob.setSportAngebot(sportAngebot);
//        buchungsJob.setSportTermin(sportTermin);
//        applicationStateDao.addSportBuchungsJob(buchungsJob);
    }

    private void addFinishDummySportBookingJob() {

    }

}
