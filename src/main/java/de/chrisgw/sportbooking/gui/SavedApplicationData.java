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


@Data
public class SavedApplicationData {

    private boolean firstVisite = true;
    private LocalDateTime saveTime = LocalDateTime.now();
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
