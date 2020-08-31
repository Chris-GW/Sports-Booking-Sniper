package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.AnimatedLabel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AnimatedClock extends AnimatedLabel {

    @Getter
    private final DateTimeFormatter timeFormatter;


    public AnimatedClock() {
        this(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public AnimatedClock(DateTimeFormatter timeFormatter) {
        super(timeFormatter.format(LocalDateTime.now().withNano(0)));
        this.timeFormatter = timeFormatter;
        startAnimation(200);
    }


    @Override
    public synchronized void nextFrame() {
        LocalDateTime time = LocalDateTime.now().withNano(0);
        setLines(new String[] { timeFormatter.format(time) });
        invalidate();
    }

    @Override
    public synchronized AnimatedClock stopAnimation() {
        // dont want to stop animation
        return this;
    }

}
