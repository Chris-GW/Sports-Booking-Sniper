package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.Label;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;


public class AnimatedClock extends Label {

    @Getter
    private final DateTimeFormatter timeFormatter;


    public AnimatedClock() {
        this(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public AnimatedClock(DateTimeFormatter timeFormatter) {
        super("");
        this.timeFormatter = requireNonNull(timeFormatter);
    }


    @Override
    public boolean isInvalid() {
        return super.isInvalid() || isClockLabelOutdated();
    }

    private boolean isClockLabelOutdated() {
        String formatNow = formatNow();
        if (!formatNow.equals(getText())) {
            setText(formatNow);
            return true;
        }
        return false;
    }


    private String formatNow() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        return timeFormatter.format(now);
    }

}
