package de.chrisgw.sportbookingsniper.gui.state;

import de.chrisgw.sportbookingsniper.buchung.Teilnehmer;

import java.util.List;


public interface TeilnehmerListeListener {

    void onChangedTeilnehmerListe(List<Teilnehmer> changedTeilnehmerListe);

}
