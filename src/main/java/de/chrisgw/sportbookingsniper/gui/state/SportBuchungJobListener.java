package de.chrisgw.sportbookingsniper.gui.state;

import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;


public interface SportBuchungJobListener {

    void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

    void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob);

    void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob);

}
