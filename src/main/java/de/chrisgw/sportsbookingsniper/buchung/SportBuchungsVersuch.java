package de.chrisgw.sportsbookingsniper.buchung;

import com.googlecode.lanterna.TerminalTextUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;


@Data
public class SportBuchungsVersuch {

    private final SportBuchungsVersuchStatus status;
    private final SportBuchungsBestaetigung buchungsBestaetigung;
    private final LocalDateTime timestamp = LocalDateTime.now();


    public static SportBuchungsVersuch newErfolgreicherBuchungsVersuch(SportBuchungsBestaetigung buchungsBestaetigung) {
        return new SportBuchungsVersuch(BUCHUNG_ERFOLGREICH, buchungsBestaetigung);
    }

    public static SportBuchungsVersuch newBuchungsVersuch(SportBuchungsVersuchStatus status) {
        return new SportBuchungsVersuch(status, null);
    }


    public enum SportBuchungsVersuchStatus {
        BUCHUNG_GESCHLOSSEN, //
        BUCHUNG_WARTELISTE, //
        BUCHUNG_ABGELAUFEN, //
        BUCHUNG_ERFOLGREICH, //
        BUCHUNG_FEHLER;


        public boolean canContineNextBuchungsVersuch() {
            switch (this) {
            case BUCHUNG_GESCHLOSSEN:
            case BUCHUNG_WARTELISTE:
                return true;
            case BUCHUNG_ABGELAUFEN:
            case BUCHUNG_ERFOLGREICH:
            case BUCHUNG_FEHLER:
                return false;
            default:
                throw new IllegalArgumentException("unknown SportBuchungsVersuchStatus" + this);
            }
        }

        public static int versuchStatusMaxLength() {
            return Arrays.stream(SportBuchungsVersuchStatus.values())
                    .map(SportBuchungsVersuchStatus::toString)
                    .mapToInt(TerminalTextUtils::getColumnWidth)
                    .max()
                    .orElse(1);
        }

        @Override
        public String toString() {
            String name = name().substring("BUCHUNG_".length());
            return StringUtils.capitalize(StringUtils.lowerCase(name));
        }

    }

}
