package de.chrisgw.sportsbookingsniper.buchung.strategie;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;

import java.time.LocalDateTime;


@JsonTypeInfo(use = Id.MINIMAL_CLASS, include = As.WRAPPER_OBJECT)
public interface SportBuchungsStrategie {

    /**
     * @param sportBuchungsJob für welchen SportBuchungsJob der nächte Versuchszeitpunkt gesucht wird.
     * @return der nächste Zeitpunkt, zu dem der übergebene SportBuchungsJob erneut überprüft werden soll.
     */
    LocalDateTime getNextTimeForCheck(SportBuchungsJob sportBuchungsJob);


}
