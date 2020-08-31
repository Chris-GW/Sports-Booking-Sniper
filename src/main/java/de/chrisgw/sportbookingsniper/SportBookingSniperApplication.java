package de.chrisgw.sportbookingsniper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.AbstractTextGUI;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.chrisgw.sportbookingsniper.angebot.HszRwthAachenSportKatalogRepository;
import de.chrisgw.sportbookingsniper.angebot.SportKatalogRepository;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsSniperService;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportbookingsniper.gui.SportBookingMainWindow;
import de.chrisgw.sportbookingsniper.gui.dialog.TeilnehmerModalDialog;
import de.chrisgw.sportbookingsniper.gui.dialog.WelcomeDialog;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class SportBookingSniperApplication {


    @Bean(destroyMethod = "shutdownNow")
    public SportBuchungsSniperService sportBookingSniperService() {
        return new SportBuchungsSniperService();
    }

    @Bean
    public SportKatalogRepository sportKatalogRepository() {
        return new HszRwthAachenSportKatalogRepository();
    }


    @Bean
    public ApplicationStateDao applicationStateDao() {
        return new ApplicationStateDao(savedApplicationDataResource(), objectMapper(), sportKatalogRepository());
    }

    @Bean
    public Resource savedApplicationDataResource() {
        return new FileSystemResource("savedSportBookingApplicationData.json");
    }


    // lanterna

    @Lazy
    @Bean(initMethod = "startScreen", destroyMethod = "stopScreen")
    public Screen guiScreen() throws IOException {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory() //
                .setInitialTerminalSize(new TerminalSize(100, 50))
                .setTerminalEmulatorTitle("Sportbuchungsbot - RWTH Hochschulsport");
        return defaultTerminalFactory.createScreen();
    }

    @Lazy
    @Bean
    public MultiWindowTextGUI multiWindowTextGUI(Screen guiScreen, ApplicationStateDao applicationStateDao) {
        MultiWindowTextGUI windowTextGUI = new MultiWindowTextGUI(guiScreen);
        windowTextGUI.setTheme(applicationStateDao.getSelectedheme());
        return windowTextGUI;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public SportBookingMainWindow sportBookingMainWindow() {
        return new SportBookingMainWindow(sportKatalogRepository(), sportBookingSniperService(), applicationStateDao());
    }

    @Bean
    public List<Theme> guiThemes() {
        registerLanternaTheme("default", "/default-theme.properties");
        registerLanternaTheme("bigsnake", "/bigsnake-theme.properties");
        registerLanternaTheme("businessmachine", "/businessmachine-theme.properties");
        registerLanternaTheme("conqueror", "/conqueror-theme.properties");
        registerLanternaTheme("defrost", "/defrost-theme.properties");
        registerLanternaTheme("blaster", "/blaster-theme.properties");
        return LanternaThemes.getRegisteredThemes()
                .stream()
                .map(LanternaThemes::getRegisteredTheme)
                .collect(Collectors.toList());
    }


    // other

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    private void registerLanternaTheme(String themeName, String resourceName) {
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


    // MAIN

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(
                SportBookingSniperApplication.class)) {
            ApplicationStateDao applicationStateDao = ctx.getBean(ApplicationStateDao.class);
            MultiWindowTextGUI windowTextGUI = ctx.getBean(MultiWindowTextGUI.class);
            if (applicationStateDao.isFirstVisite()) {
                showFirstVisiteDialog(applicationStateDao, windowTextGUI);
            }
            SportBookingMainWindow sportBookingMainWindow = ctx.getBean(SportBookingMainWindow.class);
            windowTextGUI.addWindowAndWait(sportBookingMainWindow);
            log.trace("finish SportBooking gui");
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            e.printStackTrace();
        }
    }


    private static void showFirstVisiteDialog(ApplicationStateDao applicationStateDao,
            MultiWindowTextGUI windowTextGUI) {
        log.trace("showFirstVisiteDialog");
        new WelcomeDialog().showDialog(windowTextGUI);

        TeilnehmerModalDialog teilnehmerModalDialog = new TeilnehmerModalDialog(applicationStateDao, true);
        Optional<Teilnehmer> teilnehmerAngaben = teilnehmerModalDialog.showDialog(windowTextGUI);
        applicationStateDao.addTeilnehmer(teilnehmerAngaben.orElseThrow(RuntimeException::new));
        applicationStateDao.setFirstVisite(false);
    }

}
