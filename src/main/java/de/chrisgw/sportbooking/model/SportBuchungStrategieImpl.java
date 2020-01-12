package de.chrisgw.sportbooking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SportBuchungStrategieImpl {

    public static class FixedPeriodTimeBuchungStrategie implements SportBuchungStrategie {

        /**
         * fixedPeriod in seconds
         */
        private final long fixedPeriod;
        private boolean isFirstTry;


        /**
         * @param fixedPeriod in given TimeUnit
         * @param timeUnit    timeUnit
         */
        public FixedPeriodTimeBuchungStrategie(long fixedPeriod, TimeUnit timeUnit) {
            this.fixedPeriod = Math.max(60, timeUnit.toSeconds(fixedPeriod));
            this.isFirstTry = true;
        }


        @Override
        public LocalDateTime getNextTimeForCheckTermin(SportBuchungsJob sportBuchungsJob) {
            if (isFirstTry) {
                isFirstTry = false;
                return LocalDateTime.now().plusSeconds(1);
            }
            return LocalDateTime.now().plusSeconds(fixedPeriod);
        }

    }


    public static class FixedPeriodWithSleepPeriodTimeBuchungStrategie extends FixedPeriodTimeBuchungStrategie {

        public static final Duration MAX_WAIT_DURATION = Duration.ofHours(1);

        private final LocalTime sleepStartTime;
        private final LocalTime sleepEndTime;


        public FixedPeriodWithSleepPeriodTimeBuchungStrategie(long fixedPeriod, TimeUnit timeUnit) {
            this(fixedPeriod, timeUnit, LocalTime.of(23, 0), LocalTime.of(7, 15));
        }

        public FixedPeriodWithSleepPeriodTimeBuchungStrategie(long fixedPeriod, TimeUnit timeUnit,
                LocalTime sleepStartTime, LocalTime sleepEndTime) {
            super(fixedPeriod, timeUnit);
            this.sleepStartTime = Objects.requireNonNull(sleepStartTime);
            this.sleepEndTime = Objects.requireNonNull(sleepEndTime);
        }


        @Override
        public LocalDateTime getNextTimeForCheckTermin(SportBuchungsJob sportBuchungsJob) {
            LocalDateTime nextTimeForCheckTermin = super.getNextTimeForCheckTermin(sportBuchungsJob);
            LocalTime nextTime = nextTimeForCheckTermin.toLocalTime();
            if (nextTime.isAfter(sleepStartTime) && nextTime.isBefore(sleepEndTime)) {
                Duration durationTilSleepEndTime = Duration.between(nextTime, sleepEndTime);
                if (durationTilSleepEndTime.compareTo(MAX_WAIT_DURATION) > 0) {
                    nextTimeForCheckTermin = nextTimeForCheckTermin.plus(MAX_WAIT_DURATION);
                } else {
                    nextTimeForCheckTermin = nextTimeForCheckTermin.plus(durationTilSleepEndTime);
                }
            }
            return nextTimeForCheckTermin;
        }

        public LocalTime getSleepStartTime() {
            return sleepStartTime;
        }

        public LocalTime getSleepEndTime() {
            return sleepEndTime;
        }

    }


    public static class EarliestTerminBuchungStrategiePeriod extends FixedPeriodWithSleepPeriodTimeBuchungStrategie {


        public EarliestTerminBuchungStrategiePeriod(long fixedPeriod, TimeUnit timeUnit) {
            super(fixedPeriod, timeUnit);
        }

        public EarliestTerminBuchungStrategiePeriod(long fixedPeriod, TimeUnit timeUnit, LocalTime sleepStartTime,
                LocalTime sleepEndTime) {
            super(fixedPeriod, timeUnit, sleepStartTime, sleepEndTime);
        }


        @Override
        public LocalDateTime getNextTimeForCheckTermin(SportBuchungsJob sportBuchungsJob) {
            SportTermin sportTermin = sportBuchungsJob.getSportTermin();
            LocalDateTime buchungsBeginn = sportTermin.getBuchungsBeginn();
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(buchungsBeginn)) {
                return buchungsBeginn;
            } else if (now.isBefore(buchungsBeginn.plusMinutes(10))) {
                return now.plusSeconds(30); // intensive 30s checks
            }
            return super.getNextTimeForCheckTermin(sportBuchungsJob);
        }

    }

}
