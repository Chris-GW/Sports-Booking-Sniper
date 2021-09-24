package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.ProgressBar;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;


public class CountdownProgressBar extends ProgressBar {

    private LocalDateTime countdownStartTime;
    private Duration countdownDuration;
    private long lastDrawnSecond;


    public CountdownProgressBar() {
        startCountdown(Duration.ZERO);
        setPreferredWidth(9);
    }


    public CountdownProgressBar startCountdown(LocalDateTime targetDateTime) {
        Duration countdownDuration = Duration.between(LocalDateTime.now(), targetDateTime);
        return startCountdown(countdownDuration);
    }

    public CountdownProgressBar startCountdown(Duration countdownDuration) {
        this.countdownStartTime = LocalDateTime.now();
        this.countdownDuration = requireNonNull(countdownDuration).abs();
        this.lastDrawnSecond = 0;
        invalidate();
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
            return String.format("%02dd %02dh", days, hours);
        } else if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
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
        super.onBeforeDrawing();
        lastDrawnSecond = remainingDuration().getSeconds();
    }

    @Override
    public synchronized float getProgress() {
        long elapsedSeconds = elapsedDuration().withNanos(0).getSeconds();
        long durationSeconds = countdownDuration.withNanos(0).getSeconds();
        return (elapsedSeconds * 1.0f) / durationSeconds;
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid() || lastDrawnSecond != remainingDuration().getSeconds();
    }


    @Override
    protected CountdownProgressBar self() {
        return (CountdownProgressBar) super.self();
    }

}
