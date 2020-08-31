package de.chrisgw.sportsbookingsniper.buchung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SportBuchungsStrategieImpl {

    private SportBuchungsStrategieImpl() {

    }


    public static SportBuchungsStrategie defaultSportBuchungStrategie() {
        return new EarliestTerminBuchungsStrategiePeriod(
                new WithSleepTimeBuchungsStrategie(LocalTime.of(23, 0), LocalTime.of(7, 15),
                        new FixedPeriodBuchungsStrategie(15, TimeUnit.MINUTES)));
    }


    public static class FixedPeriodBuchungsStrategie implements SportBuchungsStrategie {

        /**
         * fixedPeriod in seconds
         */
        private final long fixedPeriod;
        private boolean isFirstTry;


        /**
         * @param fixedPeriod in given TimeUnit
         * @param timeUnit    timeUnit
         */
        public FixedPeriodBuchungsStrategie(long fixedPeriod, TimeUnit timeUnit) {
            this.fixedPeriod = Math.max(60, timeUnit.toSeconds(fixedPeriod));
            this.isFirstTry = true;
        }


        @Override
        public LocalDateTime getNextTimeForCheck(SportBuchungsJob sportBuchungsJob) {
            if (isFirstTry) {
                isFirstTry = false;
                return LocalDateTime.now().plusSeconds(1);
            } else {
                return LocalDateTime.now().plusSeconds(fixedPeriod);
            }
        }

    }


    public static class WithSleepTimeBuchungsStrategie implements SportBuchungsStrategie {

        public static final Duration MAX_WAIT_DURATION = Duration.ofHours(1);

        private final SportBuchungsStrategie buchungStrategie;
        private final LocalTime sleepStartTime;
        private final LocalTime sleepEndTime;


        public WithSleepTimeBuchungsStrategie(SportBuchungsStrategie buchungStrategie) {
            this(LocalTime.of(23, 0), LocalTime.of(7, 15), buchungStrategie);
        }

        public WithSleepTimeBuchungsStrategie(LocalTime sleepStartTime, LocalTime sleepEndTime,
                SportBuchungsStrategie buchungStrategie) {
            this.sleepStartTime = Objects.requireNonNull(sleepStartTime);
            this.sleepEndTime = Objects.requireNonNull(sleepEndTime);
            this.buchungStrategie = Objects.requireNonNull(buchungStrategie);
        }


        @Override
        public LocalDateTime getNextTimeForCheck(SportBuchungsJob sportBuchungsJob) {
            LocalDateTime nextTimeForCheckTermin = buchungStrategie.getNextTimeForCheck(sportBuchungsJob);
            LocalTime nextTime = nextTimeForCheckTermin.toLocalTime();
            if (nextTime.isAfter(sleepStartTime) && nextTime.isBefore(sleepEndTime)) {
                Duration durationTilSleepEndTime = Duration.between(nextTime, sleepEndTime);
                if (durationTilSleepEndTime.compareTo(MAX_WAIT_DURATION) > 0) {
                    return nextTimeForCheckTermin.plus(MAX_WAIT_DURATION);
                } else {
                    return nextTimeForCheckTermin.plus(durationTilSleepEndTime);
                }
            } else {
                return nextTimeForCheckTermin;
            }
        }

        public LocalTime getSleepStartTime() {
            return sleepStartTime;
        }

        public LocalTime getSleepEndTime() {
            return sleepEndTime;
        }

    }


    public static class EarliestTerminBuchungsStrategiePeriod implements SportBuchungsStrategie {

        private SportBuchungsStrategie buchungStrategie;

        public EarliestTerminBuchungsStrategiePeriod(SportBuchungsStrategie buchungStrategie) {
            this.buchungStrategie = Objects.requireNonNull(buchungStrategie);
        }


        @Override
        public LocalDateTime getNextTimeForCheck(SportBuchungsJob sportBuchungsJob) {
            LocalDateTime buchungsBeginn = sportBuchungsJob.getBuchungsBeginn();
            LocalDateTime now = LocalDateTime.now();
            if (buchungsBeginn != null && now.isBefore(buchungsBeginn)) {
                return buchungsBeginn;
            } else if (buchungsBeginn != null && now.isBefore(buchungsBeginn.plusMinutes(5))) {
                return now.plusSeconds(30); // intensive 30s checks
            } else {
                return buchungStrategie.getNextTimeForCheck(sportBuchungsJob);
            }
        }

    }

}
