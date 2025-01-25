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
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.gui.SportBookingMainWindow;
import de.chrisgw.sportsbookingsniper.gui.dialog.WelcomeDialog;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.teilnehmer.TeilnehmerFormDialog;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Log4j2
@Data
public class SportBookingSniperApplication {

    public static final AtomicBoolean finalBooking = new AtomicBoolean(false);

    private final SportKatalogRepository sportKatalogRepository;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executorService;
    private final ApplicationStateDao applicationStateDao;


    public SportBookingSniperApplication() {
        this.sportKatalogRepository = new HszRwthAachenSportKatalogRepository();
        this.objectMapper = createObjectMapper();
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        threadPoolExecutor.setRemoveOnCancelPolicy(true);
        this.executorService = threadPoolExecutor;
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
            executorService.shutdown();
            applicationStateDao.getPendingBuchungsJobs()
                    .stream()
                    .map(SportBuchungsJob::getJobId)
                    .map(applicationStateDao::getScheduledSportBuchungsJob)
                    .forEach(scheduledBuchungsJob -> scheduledBuchungsJob.cancel(false));
            if (!executorService.awaitTermination(4, TimeUnit.SECONDS)) {
                List<Runnable> runnables = executorService.shutdownNow();
                log.warn("there are still running background jobs after 4s: " + runnables);
            }
        } catch (InterruptedException e) {
            log.trace("interrupted while showing gui");
            Thread.currentThread().interrupt();
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
            PrintStream errStream = IoBuilder.forLogger(LogManager.getRootLogger()).buildPrintStream();
            System.setErr(errStream);
            finalBooking.set(args.length == 0 || !Boolean.parseBoolean(args[0]));
            log.trace("start SportBookingSniperApplication gui with finalBooking: " + finalBooking.get());
            var sportBookingSniperApplication = new SportBookingSniperApplication();
            sportBookingSniperApplication.showGui();
            log.trace("finish SportBookingSniperApplication gui");
            System.exit(0);
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

}
