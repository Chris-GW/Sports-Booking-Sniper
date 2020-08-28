package de.chrisgw.sportbookingsniper.gui.state;

import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import lombok.Data;

import java.math.BigInteger;
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
    private TeilnehmerAngaben teilnehmerAngaben = new TeilnehmerAngaben();

    private List<SportAngebot> watchedSportAngebote = new ArrayList<>();
    private List<SportBuchungsJob> pendingBuchungsJobs = new ArrayList<>();
    private List<SportBuchungsJob> finishedBuchungsJobs = new ArrayList<>();


    public BigInteger totalSpendEuro() {
        return finishedBuchungsJobs.stream()
                .map(SportBuchungsJob::getPreis)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

}