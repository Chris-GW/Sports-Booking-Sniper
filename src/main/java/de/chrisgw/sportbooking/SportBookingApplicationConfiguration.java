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
import de.chrisgw.sportbooking.service.AachenSportBookingService;
import de.chrisgw.sportbooking.service.LazyLoaderFilter;
import de.chrisgw.sportbooking.service.SportBookingService;
import de.chrisgw.sportbooking.service.SportBookingSniperService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


@Configuration
public class SportBookingApplicationConfiguration {


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


    @Bean(destroyMethod = "close")
    public Screen guiScreen() throws IOException {
        return new DefaultTerminalFactory().createScreen();
    }

    public WindowBasedTextGUI windowBasedTextGUI() throws IOException {
        return new MultiWindowTextGUI(guiScreen());
    }


    @Bean
    public SportBookingGui sportBookingGui() throws IOException {
        return new SportBookingGui(sportBookingService(), objectMapper(), guiScreen(), windowBasedTextGUI());
    }


    @Bean
    public SportBookingService sportBookingService() {
        return new AachenSportBookingService();
    }

    @Bean(destroyMethod = "shutdownNow")
    public SportBookingSniperService sportBookingSniperService() {
        return new SportBookingSniperService(sportBookingService());
    }

    @Bean
    public SportBookingApplication sportBookingApplication() throws Exception {
        return new SportBookingApplication(sportBookingService(), sportBookingSniperService(), objectMapper(),
                sportBookingGui());
    }


}
