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
    }


    public void startCountdown(LocalDateTime targetDateTime) {
        startCountdown(Duration.between(LocalDateTime.now(), targetDateTime));
    }

    public void startCountdown(Duration countdownDuration) {
        this.countdownDuration = requireNonNull(countdownDuration).abs();
        this.countdownStartTime = LocalDateTime.now();
        setMin(0);
        setMax((int) countdownDuration.getSeconds());
    }


    @Override
    public synchronized String getFormattedLabel() {
        Duration remainingDuration = remainingDuration().withNanos(0);
        long seconds = remainingDuration.getSeconds();
        if (remainingDuration.toHours() > 0) {
            remainingDuration = remainingDuration.withSeconds(seconds - (seconds % 60));
        } else if (seconds % 5 > 0) {
            remainingDuration = remainingDuration.withSeconds(seconds - (seconds % 5));
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
    protected void onBeforeDrawing() {
        setValue((int) elapsedDuration().getSeconds());
        super.onBeforeDrawing();
    }


    @Override
    public boolean isInvalid() {
        return !remainingDuration().isZero() || super.isInvalid();
    }

}
