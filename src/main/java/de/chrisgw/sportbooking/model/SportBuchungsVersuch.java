package de.chrisgw.sportbooking.model;

import lombok.Data;

import java.time.LocalDateTime;

import static de.chrisgw.sportbooking.model.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;


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
    }

}
