package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.gui.dialog.PersonenAngabenDialog;
import de.chrisgw.sportbooking.gui.dialog.SportBuchungDialog;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.repository.ApplicationStateDao;
import de.chrisgw.sportbooking.repository.SportKatalogRepository;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.upperCase;


public class MainMenuBar extends SportBookingComponent {

    private final SportKatalogRepository sportKatalogRepository;

    @Getter
    private MenuBar menuBar;

    @Getter
    private Menu viewMenu;

    @Getter
    private Menu navigationMenu;


    public MainMenuBar(SportKatalogRepository sportKatalogRepository, ApplicationStateDao applicationStateDao,
            Window window) {
        super(applicationStateDao, window, "top Navigation", KeyType.F1);
        this.sportKatalogRepository = sportKatalogRepository;
        setLayoutManager(new BorderLayout());

        this.viewMenu = new Menu("View");
        this.navigationMenu = new Menu("Navigation");
        this.menuBar = new MenuBar().add(sportBuchungMenu())
                .add(viewMenu)
                .add(navigationMenu)
                .add(debugMenu())
                .add(personenAngabenMenu())
                .add(languageMenu());
        viewMenu.add(createSwitchThemeMenuItem());
        addViewMenuItemsFor(this);
        addNavigationMenuItemsFor(this);

        addComponent(menuBar, Location.CENTER);
        addComponent(clockDisplyMenuBar(), Location.RIGHT);
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
            new SportBuchungDialog(sportKatalogRepository).showDialog(getTextGUI());
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
        return new Menu("Debug");
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

}
