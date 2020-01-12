package de.chrisgw.sportbooking.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.screen.Screen;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.SportTermin.SportTerminStatus;
import de.chrisgw.sportbooking.service.SportBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;

import static de.chrisgw.sportbooking.SportBookingApplication.PERSONEN_ANGABEN_PATH;


@Slf4j
@RequiredArgsConstructor
public class SportBookingGui {

    private final SportBookingService sportBookingService;
    private final ObjectMapper objectMapper;

    private final Screen screen;
    private final WindowBasedTextGUI textGUI;
    private boolean isOn = false;

    private ActionListBox beendeteBuchungenBox;


    public void turnOn() throws IOException {
        screen.startScreen();
        isOn = true;
        drawUI();
    }

    public void turnOff() throws IOException {
        isOn = false;
        screen.stopScreen();
    }


    public boolean isOn() {
        return isOn;
    }


    private PersonenAngaben readPersonenAngaben() {
        try (BufferedReader fileReader = Files.newBufferedReader(PERSONEN_ANGABEN_PATH, StandardCharsets.UTF_8)) {
            PersonenAngaben personenAngaben = objectMapper.readValue(fileReader, PersonenAngaben.class);
            log.trace("read personenAngaben {} from {}", personenAngaben, PERSONEN_ANGABEN_PATH);
            return personenAngaben;
        } catch (IOException e) {
            throw new RuntimeException("Could not read PersonenAngaben of " + PERSONEN_ANGABEN_PATH, e);
        }
    }

    private void writePersonenAngaben(PersonenAngaben personenAngaben) {
        try (BufferedWriter fileWriter = Files.newBufferedWriter(PERSONEN_ANGABEN_PATH, StandardCharsets.UTF_8)) {
            log.trace("write personenAngaben {} to {}", personenAngaben, PERSONEN_ANGABEN_PATH);
            objectMapper.writeValue(fileWriter, personenAngaben);
        } catch (Exception e) {
            throw new RuntimeException("Could not write PersonenAngaben to file " + PERSONEN_ANGABEN_PATH, e);
        }
    }


    public void drawUI() {
        final Window window = new BasicWindow("Buchungsbot - RWTH Hochschulsport");
        window.setHints(Arrays.asList(Hint.EXPANDED));

        Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button personenangabenBtn = new Button("Personenangaben", () -> {
            PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow();
            personenAngabenWindow.bindPersonenAngaben(readPersonenAngaben());
            textGUI.addWindowAndWait(personenAngabenWindow);
            PersonenAngaben personenAngaben = personenAngabenWindow.personenAngaben();
            // TODO writePersonenAngaben(personenAngaben);
            System.out.println(personenAngaben);
        });
        contentPanel.addComponent(personenangabenBtn);


        AusstehendeSportBuchungenPanel pendingSportBookingPanel = new AusstehendeSportBuchungenPanel();

        pendingSportBookingPanel.addPendingSportBuchungsJob(getJob());
        pendingSportBookingPanel.addPendingSportBuchungsJob(getJob());
        pendingSportBookingPanel.addPendingSportBuchungsJob(getJob());
        contentPanel.addComponent(
                pendingSportBookingPanel.withBorder(Borders.singleLine("Ausstehende Sportbuchungen")));

        beendeteBuchungenBox = new ActionListBox();
        contentPanel.addComponent(beendeteBuchungenBox);


        window.setComponent(contentPanel);
        textGUI.addWindowAndWait(window);
        System.out.println("finished");
    }

    private SportBuchungsJob getJob() {
        SportArt sportArt = new SportArt("Badminton", "http://badminton.com");
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);
        sportAngebot.setKursnummer("51232");

        SportTermin sportTermin = new SportTermin();
        sportTermin.setSportAngebot(sportAngebot);
        sportTermin.setStartZeit(LocalDateTime.now().plusDays(2).minusMinutes(90));
        sportTermin.setEndZeit(sportTermin.getStartZeit().plusMinutes(90));
        sportTermin.setStatus(SportTerminStatus.OFFEN);

        PersonenAngaben personenAngaben = new PersonenAngaben();
        return new SportBuchungsJob(sportTermin, personenAngaben);
    }

}
