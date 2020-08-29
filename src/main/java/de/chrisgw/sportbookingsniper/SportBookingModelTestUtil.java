package de.chrisgw.sportbookingsniper;

import de.chrisgw.sportbookingsniper.angebot.*;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportbookingsniper.buchung.Teilnehmer.Gender;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerKategorie;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static de.chrisgw.sportbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.*;
import static java.util.Collections.singletonList;


public class SportBookingModelTestUtil {

    private SportBookingModelTestUtil() {
        throw new IllegalAccessError();
    }

    private static AtomicInteger jobIdCounter = new AtomicInteger();
    private static Supplier<LocalDateTime> futureDateTimeSupplier = newRandomDateTimeSupplier(1, true);
    private static Supplier<LocalDateTime> pastDateTimeSupplier = newRandomDateTimeSupplier(1, false);

    public static SportKatalog sportKatalog = newSportKatalog();

    public static SportKatalog newSportKatalog() {
        SportKatalog sportKatalog = new SportKatalog();
        sportKatalog.setAbrufzeitpunkt(LocalDateTime.now());
        sportKatalog.setKatalog("Text Katalog");
        sportKatalog.setZeitraumStart(pastDateTimeSupplier.get().toLocalDate());
        sportKatalog.setZeitraumEnde(futureDateTimeSupplier.get().toLocalDate());

        sportKatalog.addSportArt(newBadmintonKarteSportArt());
        sportKatalog.addSportArt(newBadmintonEinzelplatzSportArt());
        sportKatalog.addSportArt(newBadmintonEinzelTerminSportArt());
        sportKatalog.addSportArt(newVolleyballSportArt());
        sportKatalog.addSportArt(newFechtenSportArt());
        return sportKatalog;
    }

    private static SportArt newBadmintonKarteSportArt() {
        return newSportArt("Badminton Karte Text");
    }

    private static SportArt newBadmintonEinzelplatzSportArt() {
        SportArt sportArt = newSportArt("Badminton Einzelplatz Text");
        sportArt.getSportAngebote().forEach(sportAngebot -> sportAngebot.setBuchungsArt(EINZEL_PLATZ_BUCHUNG));
        return sportArt;
    }

    private static SportArt newBadmintonEinzelTerminSportArt() {
        SportArt sportArt = newSportArt("Badminton Einzeltermin Text");
        sportArt.getSportAngebote().forEach(sportAngebot -> sportAngebot.setBuchungsArt(EINZEL_TERMIN_BUCHUNG));
        return sportArt;
    }

    private static SportArt newVolleyballSportArt() {
        SportArt sportArt = newSportArt("Volleyball Level 1 Text");
        return sportArt;
    }

    private static SportArt newFechtenSportArt() {
        SportArt sportArt = newSportArt("Fechten Level 2 Text");
        return sportArt;
    }


    public static SportArt newSportArt(String name) {
        SportArt sportArt = new SportArt();
        sportArt.setName(name);
        sportArt.setUrl("https://example.com/sportArten/" + name);
        sportArt.addSportAngebot(newWeeklySportAngebot(sportArt, pastDateTimeSupplier.get()));
        sportArt.addSportAngebot(newWeeklySportAngebot(sportArt, pastDateTimeSupplier.get()));
        sportArt.addSportAngebot(newWeeklySportAngebot(sportArt, futureDateTimeSupplier.get()));
        sportArt.addSportAngebot(newWeeklySportAngebot(sportArt, futureDateTimeSupplier.get()));
        sportArt.addSportAngebot(newWeeklySportAngebot(sportArt, futureDateTimeSupplier.get()));
        return sportArt;
    }


    public static SportAngebot newWeeklySportAngebot(SportArt sportArt, LocalDateTime firstTerminDate) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);

        sportAngebot.setKursnummer(newKursnummer(sportArt, firstTerminDate));
        sportAngebot.setDetails("Angebot Details Text für " + firstTerminDate);
        sportAngebot.setLeitung("Leitung Text für " + firstTerminDate);
        sportAngebot.setOrt("Ort Text für " + firstTerminDate);
        int randomPreis = (firstTerminDate.getDayOfMonth() + 1) * 100;
        sportAngebot.setPreis(new SportAngebotPreis(randomPreis));
        sportAngebot.setBuchungsArt(ANGEBOT_TICKET_BUCHUNG);
        sportAngebot.setPasswortGesichert(false);

        SortedSet<SportTermin> sportTermine = newSportTermine(sportAngebot, firstTerminDate);
        sportAngebot.setSportTermine(sportTermine);
        sportAngebot.setZeitraumStart(sportTermine.first().getTerminDate());
        sportAngebot.setZeitraumEnde(sportTermine.last().getTerminDate());
        return sportAngebot;
    }

    private static String newKursnummer(SportArt sportArt, LocalDateTime firstTerminDate) {
        String abbreviatedSportArtName = StringUtils.abbreviate(sportArt.getName(), "-", 6);
        String terminStr = firstTerminDate.getDayOfMonth() + "-" + firstTerminDate.getMonthValue();
        return abbreviatedSportArtName + terminStr;
    }


    public static SortedSet<SportTermin> newSportTermine(SportAngebot sportAngebot, LocalDateTime firstTerminDate) {
        SortedSet<SportTermin> sportTermine = new TreeSet<>();
        for (int i = 0; i < 4; i++) {
            SportTermin sportTermin = new SportTermin();
            sportTermin.setSportAngebot(sportAngebot);
            sportTermin.setStartZeit(firstTerminDate.plusWeeks(i));
            sportTermin.setEndZeit(firstTerminDate.plusWeeks(i));
            sportTermine.add(sportTermin);
        }
        return sportTermine;
    }


    public static Teilnehmer newTeilnehmer() {
        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVorname("Vorname Text");
        teilnehmer.setNachname("Nachname Test");
        teilnehmer.setStreet("Street Test");
        teilnehmer.setOrt("Ort Test");
        teilnehmer.setEmail("Email@text.de");
        teilnehmer.setTelefon("telefonnummer");
        teilnehmer.setGender(Gender.FEMALE);

        teilnehmer.setTeilnehmerKategorie(TeilnehmerKategorie.MITARBEITER_FH);
        teilnehmer.setMitarbeiterNummer("MitarbeiterNummer Text");
        teilnehmer.setMatrikelnummer("Matrikelnummer Text");

        teilnehmer.setIban("DE92 24012 51235 213523");
        return teilnehmer;
    }

    public static SportBuchungsJob newSportBuchungsJob() {
        int jobId = jobIdCounter.incrementAndGet();
        Set<SportArt> sportArten = sportKatalog.getSportArten();
        SportArt sportArt = sportArten.stream()
                .skip(jobId % sportArten.size())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        Set<SportAngebot> sportAngebote = sportArt.getSportAngebote();
        SportAngebot sportAngebot = sportAngebote.stream()
                .skip(jobId % sportAngebote.size())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        SortedSet<SportTermin> sportTermine = sportAngebot.getSportTermine();
        SportTermin sportTermin = sportTermine.stream()
                .skip(jobId % sportTermine.size())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        SportBuchungsJob buchungsJob = new SportBuchungsJob();
        buchungsJob.setJobId(jobId);
        buchungsJob.setTeilnehmerListe(singletonList(newTeilnehmer()));
        buchungsJob.setSportTermin(sportTermin);
        return buchungsJob;
    }


    private static Supplier<LocalDateTime> newRandomDateTimeSupplier(long seed, boolean generateFutureDateTime) {
        Random random = new Random(seed);
        LocalDateTime now = LocalDate.now().atTime(LocalTime.of(10, 0));
        return () -> {
            if (generateFutureDateTime) {
                return now.plusDays(random.nextInt(99) + 1).plusHours(random.nextInt(60 / 15) * 15);
            } else {
                return now.minusDays(random.nextInt(99) + 1).plusHours(random.nextInt(60 / 15) * 15);
            }
        };
    }

}
