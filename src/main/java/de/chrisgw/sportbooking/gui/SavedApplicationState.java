package de.chrisgw.sportbooking.gui;

import de.chrisgw.sportbooking.model.PersonenAngaben;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportBuchungsJob;
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
    private PersonenAngaben personenAngaben = new PersonenAngaben();

    private List<SportAngebot> watchedSportAngebote = new ArrayList<>();
    private List<SportBuchungsJob> pendingBuchungsJobs = new ArrayList<>();
    private List<SportBuchungsBestaetigung> finishedBuchungsJobs = new ArrayList<>();


    public BigInteger totalSpendEuro() {
        return finishedBuchungsJobs.stream()
                .map(SportBuchungsBestaetigung::getPreis)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

}
