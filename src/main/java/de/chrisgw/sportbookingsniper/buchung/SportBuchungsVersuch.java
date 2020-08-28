package de.chrisgw.sportbookingsniper.buchung;

import lombok.Data;

import java.time.LocalDateTime;

import static de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;


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
        BUCHUNG_GESCHLOSSEN, BUCHUNG_WARTELISTE, BUCHUNG_ABGELAUFEN, BUCHUNG_ERFOLGREICH, BUCHUNG_FEHLGESCHLAGEN;

        public boolean canContineNextBuchungsVersuch() {
            switch (this) {
            case BUCHUNG_GESCHLOSSEN:
            case BUCHUNG_WARTELISTE:
                return true;
            case BUCHUNG_ABGELAUFEN:
            case BUCHUNG_ERFOLGREICH:
            case BUCHUNG_FEHLGESCHLAGEN:
                return false;
            default:
                throw new IllegalArgumentException("unknown SportBuchungsVersuchStatus" + this);
            }
        }

    }

}
