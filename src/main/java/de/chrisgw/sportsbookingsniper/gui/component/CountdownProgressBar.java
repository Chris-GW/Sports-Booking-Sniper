package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.ProgressBar;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;


public class CountdownProgressBar extends ProgressBar {

    private LocalDateTime countdownStartTime;
    private Duration countdownDuration;


    public CountdownProgressBar() {
        startCountdown(Duration.ZERO);
        setPreferredWidth(9);
    }


    public CountdownProgressBar startCountdown(LocalDateTime targetDateTime) {
        startCountdown(Duration.between(LocalDateTime.now(), targetDateTime));
        return self();
    }

    public CountdownProgressBar startCountdown(Duration countdownDuration) {
        this.countdownStartTime = LocalDateTime.now();
        this.countdownDuration = requireNonNull(countdownDuration).abs();
        setMin(0);
        setMax((int) countdownDuration.getSeconds());
        return self();
    }


    @Override
    public synchronized String getFormattedLabel() {
        Duration remainingDuration = remainingDuration().withNanos(0);
        long seconds = remainingDuration.getSeconds();
        if (remainingDuration.toHours() > 0) {
            remainingDuration = remainingDuration.withSeconds(seconds - (seconds % 60));
        }
        long effectiveTotalSecs = remainingDuration.getSeconds();
        int days = (int) (remainingDuration.toHours() / 24);
        int hours = (int) (remainingDuration.toHours() % 24);
        int minutes = (int) ((effectiveTotalSecs % (24 * 60)) / 60);
        int secs = (int) (effectiveTotalSecs % 60);
        if (days > 0) {
            return String.format("%dd %02dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        } else {
            return String.format("%02dm %02ds", minutes, secs);
        }
    }


    public Duration remainingDuration() {
        Duration remainingDuration = countdownDuration.minus(elapsedDuration());
        if (remainingDuration.isNegative()) {
            remainingDuration = Duration.ZERO;
        }
        return remainingDuration;
    }

    public Duration elapsedDuration() {
        return Duration.between(countdownStartTime, LocalDateTime.now());
    }


    @Override
    public boolean isInvalid() {
        Duration elapsedDuration = elapsedDuration().withNanos(0);
        setValue((int) elapsedDuration.getSeconds());
        return super.isInvalid();
    }


    @Override
    protected CountdownProgressBar self() {
        return (CountdownProgressBar) super.self();
    }

}
