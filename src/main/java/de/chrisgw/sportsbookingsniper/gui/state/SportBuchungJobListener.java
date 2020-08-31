package de.chrisgw.sportsbookingsniper.gui.state;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;


public interface SportBuchungJobListener {

    void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

    void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

    void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob);

}
