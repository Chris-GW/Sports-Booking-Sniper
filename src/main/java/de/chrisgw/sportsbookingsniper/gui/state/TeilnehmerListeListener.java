package de.chrisgw.sportsbookingsniper.gui.state;

import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;

import java.util.List;


public interface TeilnehmerListeListener {

    void onChangedTeilnehmerListe(List<Teilnehmer> changedTeilnehmerListe);

}
