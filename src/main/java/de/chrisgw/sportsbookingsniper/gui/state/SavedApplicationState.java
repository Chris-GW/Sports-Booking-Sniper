package de.chrisgw.sportsbookingsniper.gui.state;

import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Data
public class SavedApplicationState implements Serializable {

    private static final long serialVersionUID = 2L;

    private long versionUID = SavedApplicationState.serialVersionUID;
    private int nextJobId = 0;
    private boolean firstVisite = true;
    private Instant saveTime = Instant.now();
    private Locale language = Locale.getDefault();
    private String selectedTheme = "default";

    private List<Teilnehmer> teilnehmerListe = new ArrayList<>();
    private List<SportAngebot> watchedSportAngebote = new ArrayList<>();
    private List<SportBuchungsJob> pendingBuchungsJobs = new ArrayList<>();
    private List<SportBuchungsJob> finishedBuchungsJobs = new ArrayList<>();


    public int nextJobId() {
        return ++nextJobId;
    }

}
