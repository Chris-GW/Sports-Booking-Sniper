package de.chrisgw.sportsbookingsniper;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.SportBookingMainWindow;
import de.chrisgw.sportsbookingsniper.gui.dialog.WelcomeDialog;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.teilnehmer.TeilnehmerFormDialog;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Log4j2
@Data
public class SportBookingSniperApplication {

    private final SportKatalogRepository sportKatalogRepository;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executorService;
    private final ApplicationStateDao applicationStateDao;


    public SportBookingSniperApplication() {
        this.sportKatalogRepository = new HszRwthAachenSportKatalogRepository();
        this.objectMapper = createObjectMapper();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.applicationStateDao = new ApplicationStateDao(sportKatalogRepository, objectMapper, executorService);
    }


    public static ObjectMapper createObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return objectMapper;
    }


    public void showGui() throws IOException {
        try (var terminalScreen = createTerminalScreen()) {
            terminalScreen.startScreen();
            var multiWindowTextGUI = createMultiWindowTextGUI(terminalScreen);
            Locale.setDefault(applicationStateDao.getLanguage());
            multiWindowTextGUI.setTheme(applicationStateDao.getSelectedTheme());
            if (applicationStateDao.isFirstVisite()) {
                showFirstVisiteDialog(multiWindowTextGUI);
            }
            multiWindowTextGUI.addWindowAndWait(createSportBookingMainWindow());
        }
    }

    private TerminalScreen createTerminalScreen() throws IOException {
        var defaultTerminalFactory = new DefaultTerminalFactory() //
                .setInitialTerminalSize(new TerminalSize(100, 50))
                .setTerminalEmulatorTitle("Sportbuchungsbot - RWTH Hochschulsport");
        return defaultTerminalFactory.createScreen();
    }

    private MultiWindowTextGUI createMultiWindowTextGUI(TerminalScreen guiScreen) {
        var windowTextGUI = new MultiWindowTextGUI(guiScreen);
        registerLanternaThemes();
        windowTextGUI.setTheme(applicationStateDao.getSelectedTheme());
        return windowTextGUI;
    }


    private void showFirstVisiteDialog(MultiWindowTextGUI multiWindowTextGUI) {
        log.traceEntry("showFirstVisiteDialog");
        new WelcomeDialog().showDialog(multiWindowTextGUI);

        var teilnehmerFormDialog = new TeilnehmerFormDialog();
        teilnehmerFormDialog.setForceValidTeilnehmerForm(true);
        Optional<Teilnehmer> teilnehmer = teilnehmerFormDialog.showDialog(multiWindowTextGUI);
        applicationStateDao.setFirstVisite(false);
        applicationStateDao.setDefaultTeilnehmer(teilnehmer.orElseThrow());
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
            var properties = new Properties();
            properties.load(resourceAsStream);
            var propertyTheme = new PropertyTheme(properties);
            LanternaThemes.registerTheme(themeName, propertyTheme);
        } catch (IOException e) {
            throw new RuntimeException("could not load PropertyTheme", e);
        }
    }


    public static void main(String[] args) {
        try {
            PrintStream errStream = IoBuilder.forLogger(SportBookingSniperApplication.class).buildPrintStream();
            System.setErr(errStream);
            var chromedriverPath = Paths.get("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
            if (Files.exists(chromedriverPath)) {
                System.setProperty("webdriver.chrome.driver", chromedriverPath.toString());
            }
            log.trace("start SportBookingSniperApplication gui");
            var sportBookingSniperApplication = new SportBookingSniperApplication();
            sportBookingSniperApplication.showGui();
            log.trace("finish SportBookingSniperApplication gui");
            sportBookingSniperApplication.getExecutorService().shutdown();
            System.exit(0);
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            e.printStackTrace();
        }
    }

}
