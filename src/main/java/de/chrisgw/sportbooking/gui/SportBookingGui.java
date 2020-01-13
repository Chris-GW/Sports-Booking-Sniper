package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.SportTermin.SportTerminStatus;
import de.chrisgw.sportbooking.service.SavedApplicationDataService;
import de.chrisgw.sportbooking.service.SportBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.googlecode.lanterna.gui2.Borders.doubleLineBevel;


@Slf4j
@RequiredArgsConstructor
public class SportBookingGui {

    private final SportBookingService sportBookingService;
    private final SavedApplicationDataService savedApplicationDataService;

    private final Screen screen;
    private final WindowBasedTextGUI textGUI;

    private ActionListBox beendeteBuchungenBox;


    public void startGui() throws IOException {
        screen.startScreen();
        drawUI();
    }


    public void drawUI() {
        final Window window = new BasicWindow("Buchungsbot - RWTH Hochschulsport");
        window.setHints(Arrays.asList(Hint.FULL_SCREEN));

        Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button personenangabenBtn = new Button("Personenangaben", this::showPersonenAngabenModal);
        contentPanel.addComponent(personenangabenBtn);

        FinishedSportBuchungenPanel finishedSportBookingPanel = new FinishedSportBuchungenPanel(
                savedApplicationDataService);
        contentPanel.addComponent(finishedSportBookingPanel.withBorder(doubleLineBevel("Beendete Sportbuchungen")));

        beendeteBuchungenBox = new ActionListBox();
        contentPanel.addComponent(beendeteBuchungenBox);

        window.setComponent(contentPanel);

        if (savedApplicationDataService.getSavedApplicationData().isFirstVisite()) {
            showFirstVisiteDialog();
        }
        textGUI.addWindowAndWait(window);
        System.out.println("finished");
    }

    private void showFirstVisiteDialog() {
        new MessageDialogBuilder().setExtraWindowHints(List.of(Hint.CENTERED, Hint.FIT_TERMINAL_WINDOW))
                .setTitle("Willkommen zum \"Buchungsbot - RWTH Hochschulsport\"")
                .setText("Sie haben zum ersten mal diese Program ausgeführt. "
                        + "Daher hier eine kleine Hilfestellung:\n" + "- Mit Escape können Sie Fenster schließen\n"
                        + "- Als nächstes sollten Sie Ihre PersonenAngaben festlegen")
                .addButton(MessageDialogButton.Continue)
                .build()
                .showDialog(textGUI);
        PersonenAngaben personenAngaben = savedApplicationDataService.getSavedApplicationData().getPersonenAngaben();
        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(personenAngaben, true);
        textGUI.addWindowAndWait(personenAngabenWindow);
        personenAngabenWindow.getPersonenAngaben().ifPresent(savedApplicationDataService::updatePersonenAngaben);
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


    private void showPersonenAngabenModal() {
        SavedApplicationData savedApplicationData = savedApplicationDataService.getSavedApplicationData();
        PersonenAngaben savedPersonenAngaben = savedApplicationData.getPersonenAngaben();
        PersonenAngabenWindow personenAngabenWindow = new PersonenAngabenWindow(savedPersonenAngaben);
        savedApplicationDataService.addPersonenAngabenListener(personenAngabenWindow);
        textGUI.addWindowAndWait(personenAngabenWindow);
        savedApplicationDataService.removePersonenAngabenListener(personenAngabenWindow);
        personenAngabenWindow.getPersonenAngaben().ifPresent(savedApplicationDataService::updatePersonenAngaben);
    }

}
