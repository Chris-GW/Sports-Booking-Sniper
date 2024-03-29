package de.chrisgw.sportsbookingsniper.buchung.strategie;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Data
public class KonfigurierbareSportBuchungsStrategie implements SportBuchungsStrategie {

    private boolean firstTry = true;
    private LocalDateTime lastTry = null;
    private Duration defaultDelay = Duration.ofMinutes(10);
    private List<DefinedDelayIntervall> definedDelayIntervalls = new ArrayList<>();


    public static SportBuchungsStrategie defaultKonfiguration() {
        var sportBuchungsStrategie = new KonfigurierbareSportBuchungsStrategie();
        sportBuchungsStrategie.add(LocalTime.MIN, LocalTime.of(7, 15), Duration.ofMinutes(60));
        sportBuchungsStrategie.add(LocalTime.of(23, 0), LocalTime.MAX, Duration.ofMinutes(60));
        return sportBuchungsStrategie;
    }


    @Override
    public LocalDateTime getNextTimeForCheck(SportBuchungsJob sportBuchungsJob) {
        LocalDateTime now = LocalDateTime.now();
        if (isAfterLastTry(now)) {
            return null;
        } else if (isFirstTry()) {
            setFirstTry(false);
            return now.plusSeconds(10);
        } else if (isBeforeBuchungsBeginn(sportBuchungsJob, now)) {
            return sportBuchungsJob.getBuchungsBeginn();
        } else if (isBuchungsBeginn(sportBuchungsJob, now)) {
            return now.plusSeconds(30); // intensive 30s checks
        } else {
            Duration delay = findDefinedDelayFor(now);
            return now.plus(delay);
        }
    }


    private boolean isAfterLastTry(LocalDateTime now) {
        return lastTry != null && now.isAfter(lastTry);
    }

    private boolean isBeforeBuchungsBeginn(SportBuchungsJob sportBuchungsJob, LocalDateTime now) {
        LocalDateTime buchungsBeginn = sportBuchungsJob.getBuchungsBeginn();
        return buchungsBeginn != null && now.isBefore(buchungsBeginn);
    }

    private boolean isBuchungsBeginn(SportBuchungsJob sportBuchungsJob, LocalDateTime now) {
        LocalDateTime buchungsBeginn = sportBuchungsJob.getBuchungsBeginn();
        return buchungsBeginn != null && now.isAfter(buchungsBeginn) && now.isBefore(buchungsBeginn.plusMinutes(5));
    }

    private Duration findDefinedDelayFor(LocalDateTime now) {
        return definedDelayIntervalls.stream()
                .filter(definedDelayIntervall -> definedDelayIntervall.isInsideIntervall(now))
                .findFirst()
                .map(DefinedDelayIntervall::getDelay)
                .orElse(defaultDelay);
    }


    public void add(LocalTime from, LocalTime to, Duration delay) {
        add(new DefinedDelayIntervall(from, to, delay));
    }

    public void add(DefinedDelayIntervall definedDelayIntervall) {
        definedDelayIntervalls.add(definedDelayIntervall);
        definedDelayIntervalls.sort(Comparator.naturalOrder());
    }

    public boolean remove(DefinedDelayIntervall definedDelayIntervall) {
        return definedDelayIntervalls.remove(definedDelayIntervall);
    }


}
