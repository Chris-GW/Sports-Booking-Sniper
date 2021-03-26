package de.chrisgw.sportsbookingsniper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.AbstractTextGUI;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalogRepository;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsSniperService;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.SportBookingMainWindow;
import de.chrisgw.sportsbookingsniper.gui.dialog.TeilnehmerFormDialog;
import de.chrisgw.sportsbookingsniper.gui.dialog.WelcomeDialog;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;


@Log4j2
@Data
public class SportBookingSniperApplication {

    private final SportKatalogRepository sportKatalogRepository;
    private final SportBuchungsSniperService sniperService;
    private final ApplicationStateDao applicationStateDao;
    private final ObjectMapper objectMapper;


    public SportBookingSniperApplication() {
        this.sportKatalogRepository = new HszRwthAachenSportKatalogRepository();
        this.sniperService = new SportBuchungsSniperService();
        this.objectMapper = createObjectMapper();
        this.applicationStateDao = new ApplicationStateDao(sportKatalogRepository, sniperService, objectMapper);
    }


    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }


    public void showGui() throws IOException {
        try (TerminalScreen terminalScreen = createTerminalScreen()) {
            terminalScreen.startScreen();
            MultiWindowTextGUI multiWindowTextGUI = createMultiWindowTextGUI(terminalScreen);
            if (applicationStateDao.isFirstVisite()) {
                showFirstVisiteDialog(multiWindowTextGUI);
            }
            SportBookingMainWindow sportBookingMainWindow = createSportBookingMainWindow();
            multiWindowTextGUI.addWindowAndWait(sportBookingMainWindow);
        }
    }

    private TerminalScreen createTerminalScreen() throws IOException {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory() //
                .setInitialTerminalSize(new TerminalSize(100, 50))
                .setTerminalEmulatorTitle("Sportbuchungsbot - RWTH Hochschulsport");
        return defaultTerminalFactory.createScreen();
    }

    private MultiWindowTextGUI createMultiWindowTextGUI(TerminalScreen guiScreen) {
        MultiWindowTextGUI windowTextGUI = new MultiWindowTextGUI(guiScreen);
        registerLanternaThemes();
        windowTextGUI.setTheme(applicationStateDao.getSelectedheme());
        return windowTextGUI;
    }


    private void showFirstVisiteDialog(MultiWindowTextGUI multiWindowTextGUI) {
        log.traceEntry("showFirstVisiteDialog");
        new WelcomeDialog().showDialog(multiWindowTextGUI);

        TeilnehmerFormDialog teilnehmerFormDialog = new TeilnehmerFormDialog();
        teilnehmerFormDialog.setForceValidTeilnehmerForm(true);
        Optional<Teilnehmer> teilnehmer = teilnehmerFormDialog.showDialog(multiWindowTextGUI);
        applicationStateDao.setDefaultTeilnehmer(teilnehmer.orElseThrow());
        applicationStateDao.setFirstVisite(false);
    }

    private SportBookingMainWindow createSportBookingMainWindow() {
        return new SportBookingMainWindow(applicationStateDao);
    }


    private void registerLanternaThemes() {
        registerLanternaPropertyTheme("default", "/default-theme.properties");
        registerLanternaPropertyTheme("bigsnake", "/bigsnake-theme.properties");
        registerLanternaPropertyTheme("businessmachine", "/businessmachine-theme.properties");
        registerLanternaPropertyTheme("conqueror", "/conqueror-theme.properties");
        registerLanternaPropertyTheme("defrost", "/defrost-theme.properties");
        registerLanternaPropertyTheme("blaster", "/blaster-theme.properties");
    }

    private void registerLanternaPropertyTheme(String themeName, String resourceName) {
        if (LanternaThemes.getRegisteredTheme(themeName) != null) {
            return;
        }
        try (InputStream resourceAsStream = AbstractTextGUI.class.getResourceAsStream(resourceName)) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            PropertyTheme propertyTheme = new PropertyTheme(properties);
            LanternaThemes.registerTheme(themeName, propertyTheme);
        } catch (IOException e) {
            throw new RuntimeException("could not load PropertyTheme", e);
        }
    }


    public static void main(String[] args) {
        try {
            Path chromedriverPath = Paths.get("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
            if (Files.exists(chromedriverPath)) {
                System.setProperty("webdriver.chrome.driver", chromedriverPath.toString());
            }
            log.trace("start SportBookingSniperApplication gui");
            new SportBookingSniperApplication().showGui();
            log.trace("finish SportBookingSniperApplication gui");
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            e.printStackTrace();
        }
    }

}
