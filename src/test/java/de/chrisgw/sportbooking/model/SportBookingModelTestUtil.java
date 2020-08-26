package de.chrisgw.sportbooking.model;

import de.chrisgw.sportbooking.model.TeilnehmerAngaben.Gender;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;


public class SportBookingModelTestUtil {

    private SportBookingModelTestUtil() {
        throw new IllegalAccessError();
    }


    public static TeilnehmerAngaben createPersonenAngaben() {
        TeilnehmerAngaben teilnehmerAngaben = new TeilnehmerAngaben();
        teilnehmerAngaben.setVorname("Vorname");
        teilnehmerAngaben.setNachname("Nachname");
        teilnehmerAngaben.setEmail("Email");
        teilnehmerAngaben.setGender(Gender.FEMALE);

        teilnehmerAngaben.setStreet("Street");
        teilnehmerAngaben.setOrt("Ort");

        teilnehmerAngaben.setTeilnehmerKategorie(TeilnehmerKategorie.MITARBEITER_FH);
        teilnehmerAngaben.setMitarbeiterNummer("MitarbeiterNummer");
        teilnehmerAngaben.setMatrikelnummer("Matrikelnummer");
        return teilnehmerAngaben;
    }


    public static SportAngebot createMontagsSportAngebot(SportArt sportArt) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setKursnummer("1234");
        sportAngebot.setDetails("Angebot Montags");
        sportAngebot.setLeitung("Leitung Montags");
        sportAngebot.setOrt("Ort Montags");
        sportAngebot.setPreis(new SportAngebotPreis(2500));
        sportAngebot.setZeitraumStart(LocalDate.of(2018,4,21));
        sportAngebot.setZeitraumEnde(LocalDate.of(2018,5,21));
        sportAngebot.setSportTermine(createSportTermine(sportAngebot, LocalDate.of(2016, 10, 24)));
        return sportAngebot;
    }

    public static SportAngebot createFreitagsSportAngebot(SportArt sportArt) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setKursnummer("6412");
        sportAngebot.setDetails("Angebot Freitags");
        sportAngebot.setLeitung("Leitung Freitags");
        sportAngebot.setOrt("Ort Freitags");
        sportAngebot.setPreis(new SportAngebotPreis(1500));
        sportAngebot.setZeitraumStart(LocalDate.of(2018,4,21));
        sportAngebot.setZeitraumEnde(LocalDate.of(2018,5,21));
        sportAngebot.setSportTermine(createSportTermine(sportAngebot, LocalDate.of(2016, 10, 21)));
        return sportAngebot;
    }


    public static Set<SportTermin> createSportTermine(SportAngebot sportAngebot, LocalDate firstTerminDate) {
        Set<SportTermin> sportTermine = new TreeSet<>();
        for (int i = 0; i < 3; i++) {
            SportTermin sportTermin = new SportTermin();
            sportTermin.setSportAngebot(sportAngebot);
            sportTermin.setStartZeit(firstTerminDate.plusWeeks(i).atTime(18, 30));
            sportTermin.setEndZeit(firstTerminDate.plusWeeks(i).atTime(20, 15));
            sportTermine.add(sportTermin);
        }
        return sportTermine;
    }

}
