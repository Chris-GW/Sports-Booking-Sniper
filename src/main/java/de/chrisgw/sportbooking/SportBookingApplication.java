package de.chrisgw.sportbooking;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import de.chrisgw.sportbooking.gui.SportBookingGui;
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
import java.util.Locale;


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
    public SavedApplicationDataService savedApplicationDataService() {
        return new SavedApplicationDataService(savedApplicationDataResource(), objectMapper());
    }

    @Bean
    public Resource savedApplicationDataResource() {
        return new FileSystemResource("savedSportBookingApplicationData.json");
    }


    // SportBookingGui

    @Bean
    public SportBookingGui sportBookingGui() throws IOException {
        return new SportBookingGui(sportBookingService(), savedApplicationDataService(), guiScreen(),
                windowBasedTextGUI());
    }

    @Bean
    public WindowBasedTextGUI windowBasedTextGUI() throws IOException {
        return new MultiWindowTextGUI(guiScreen());
    }

    @Bean(destroyMethod = "close")
    public Screen guiScreen() throws IOException {
        return new DefaultTerminalFactory().createScreen();
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


    // MAIN

    public static void main(String[] args) {
        log.trace("Start SportBookingApplication ...");
        try (ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SportBookingApplication.class)) {
            Locale.setDefault(Locale.GERMANY);
            SportBookingGui sportbookingapplication = applicationContext.getBean(SportBookingGui.class);
            sportbookingapplication.startGui();
        } catch (Exception e) {
            log.error("Exception happens", e);
            e.printStackTrace();
        }
    }

}
