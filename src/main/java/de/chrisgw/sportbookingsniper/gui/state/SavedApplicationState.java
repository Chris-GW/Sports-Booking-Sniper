package de.chrisgw.sportbookingsniper.gui.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Data
public class SavedApplicationState {

    private boolean firstVisite = true;
    private LocalDateTime saveTime = LocalDateTime.now();
    private Locale language = Locale.getDefault();
    private String selectedTheme = "default";

    private List<Teilnehmer> teilnehmerListe = new ArrayList<>();
    private List<SportAngebot> watchedSportAngebote = new ArrayList<>();

    @JsonIgnore
    private List<SportBuchungsJob> pendingBuchungsJobs = new ArrayList<>();

    @JsonIgnore
    private List<SportBuchungsJob> finishedBuchungsJobs = new ArrayList<>();

}
