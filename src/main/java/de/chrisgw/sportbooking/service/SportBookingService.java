package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportBuchungsBestaetigung;
import de.chrisgw.sportbooking.model.SportBuchungsJob;


public interface SportBookingService {

    SportBuchungsBestaetigung versucheVerbindlichZuBuchen(SportBuchungsJob sportBuchungsJob);

}
