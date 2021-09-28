package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.ProgressBar;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

import java.time.Duration;
import java.time.LocalDateTime;


public class CountdownProgressBar extends ProgressBar {

    private LocalDateTime countdownStartTime;
    private Duration countdownDuration;
    private long lastDrawnSecond;


    public CountdownProgressBar() {
        startCountdown(Duration.ZERO);
        setPreferredWidth(9);
    }


    public synchronized CountdownProgressBar startCountdown(LocalDateTime targetDateTime) {
        Duration countdownDuration = Duration.between(LocalDateTime.now(), targetDateTime);
        return startCountdown(countdownDuration);
    }

    public synchronized CountdownProgressBar startCountdown(Duration countdownDuration) {
        if (countdownDuration == null || countdownDuration.isNegative()) {
            countdownDuration = Duration.ZERO;
        }
        this.countdownStartTime = LocalDateTime.now();
        this.countdownDuration = countdownDuration;
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
        return countdownDuration.minus(elapsedDuration());
    }

    public Duration elapsedDuration() {
        Duration duration = Duration.between(countdownStartTime, LocalDateTime.now());
        if (duration.compareTo(countdownDuration) < 0) {
            return duration;
        } else {
            return countdownDuration;
        }
    }


    @Override
    public synchronized float getProgress() {
        long durationSeconds = countdownDuration.getSeconds();
        if (durationSeconds == 0) {
            return 1.0f;
        }
        long elapsedSeconds = elapsedDuration().getSeconds();
        return (elapsedSeconds * 1.0f) / durationSeconds;
    }


    @Override
    protected void onAfterDrawing(TextGUIGraphics graphics) {
        super.onAfterDrawing(graphics);
        lastDrawnSecond = elapsedDuration().getSeconds();
    }


    @Override
    public boolean isInvalid() {
        return super.isInvalid() || lastDrawnSecond != elapsedDuration().getSeconds();
    }


    @Override
    protected CountdownProgressBar self() {
        return (CountdownProgressBar) super.self();
    }

}
