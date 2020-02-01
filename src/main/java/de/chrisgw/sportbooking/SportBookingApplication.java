package de.chrisgw.sportbooking;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.AbstractTextGUI;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.chrisgw.sportbooking.gui.SportBookingMainWindow;
import de.chrisgw.sportbooking.gui.component.PersonenAngabenDialog;
import de.chrisgw.sportbooking.gui.component.WelcomeDialog;
import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class SportBookingApplication {


    @Bean(destroyMethod = "shutdownNow")
    public SportBookingSniperService sportBookingSniperService() {
        return new SportBookingSniperService(sportBookingService());
    }

    @Bean
    public SportBookingService sportBookingService() {
        return new AachenSportBookingService();
    }


    @Bean
    public ApplicationStateDao savedApplicationDataService() {
        return new ApplicationStateDao(savedApplicationDataResource(), objectMapper());
    }

    @Bean
    public Resource savedApplicationDataResource() {
        return new FileSystemResource("savedSportBookingApplicationData.json");
    }


    // Lanterna GUI

    @Bean(destroyMethod = "close")
    public Screen guiScreen() throws IOException {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory() //
                .setInitialTerminalSize(new TerminalSize(100, 50))
                .setTerminalEmulatorTitle("Sportbuchungsbot - RWTH Hochschulsport");
        return defaultTerminalFactory.createScreen();
    }

    // other

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LazyLoaderFilter lazyLoaderFilter = new LazyLoaderFilter();
        FilterProvider filters = new SimpleFilterProvider().addFilter("lazyLoaderFilter", lazyLoaderFilter);
        objectMapper.setFilterProvider(filters);
        return objectMapper;
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
        System.out.println("Start SportBookingApplication ...");
        try (ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SportBookingApplication.class)) {
            Locale.setDefault(Locale.GERMANY);

            SportBookingService bookingService = applicationContext.getBean(SportBookingService.class);
            SportBookingSniperService bookingSniperService = applicationContext.getBean(
                    SportBookingSniperService.class);
            ApplicationStateDao applicationStateDao = applicationContext.getBean(ApplicationStateDao.class);
            Screen guiScreen = applicationContext.getBean(Screen.class);

            MultiWindowTextGUI windowTextGUI = new MultiWindowTextGUI(guiScreen);
            guiScreen.startScreen();
            SportBookingMainWindow sportBookingMainWindow = new SportBookingMainWindow(bookingService,
                    bookingSniperService, applicationStateDao);
            windowTextGUI.addWindow(sportBookingMainWindow);
            if (applicationStateDao.isFirstVisite()) {
                showFirstVisiteDialog(applicationStateDao, windowTextGUI);
            }
            windowTextGUI.waitForWindowToClose(sportBookingMainWindow);
            guiScreen.stopScreen();
            System.out.println("finish SportBookingApplication");
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            e.printStackTrace();
        }
    }


    private static void showFirstVisiteDialog(ApplicationStateDao applicationStateDao,
            MultiWindowTextGUI windowTextGUI) {
        new WelcomeDialog().showDialog(windowTextGUI);

        PersonenAngabenDialog personenAngabenDialog = new PersonenAngabenDialog(applicationStateDao, true);
        Optional<PersonenAngaben> personenAngaben = personenAngabenDialog.showDialog(windowTextGUI);
        applicationStateDao.updatePersonenAngaben(personenAngaben.orElseThrow(RuntimeException::new));
        applicationStateDao.setFirstVisite(false);
    }


}
