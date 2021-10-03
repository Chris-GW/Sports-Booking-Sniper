package de.chrisgw.sportsbookingsniper;

import de.chrisgw.sportsbookingsniper.angebot.*;
import de.chrisgw.sportsbookingsniper.buchung.*;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus;
import de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.*;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_ERFOLGREICH;
import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_WARTELISTE;
import static java.util.Collections.singletonList;


public class SportBookingModelTestUtil {

    private SportBookingModelTestUtil() {
        throw new IllegalAccessError();
    }

    private static final Random random = new Random(1);
    private static final AtomicInteger jobIdCounter = new AtomicInteger();
    private static final Supplier<LocalDateTime> futureDateTimeSupplier = newRandomDateTimeSupplier(true);
    private static final Supplier<LocalDateTime> pastDateTimeSupplier = newRandomDateTimeSupplier(false);

    private static final SportKatalog sportKatalog = newSportKatalog();

    public static SportKatalog newSportKatalog() {
        SportKatalog sportKatalog = SportKatalog.builder()
                .katalog("Text Katalog")
                .zeitraumStart(pastDateTimeSupplier.get().toLocalDate())
                .zeitraumEnde(futureDateTimeSupplier.get().toLocalDate())
                .build();

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
        return newSportArt("Volleyball Level 1 Text");
    }

    private static SportArt newFechtenSportArt() {
        return newSportArt("Fechten Level 2 Text");
    }


    public static SportArt newSportArt(String name) {
        String url = "https://example.com/sportArten/" + name;
        SportArt sportArt = new SportArt(name, url);
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

        String kursnummer = newKursnummer(sportArt, firstTerminDate);
        sportAngebot.setKursnummer(kursnummer);
        sportAngebot.setDetails("Angebot Details Text für " + kursnummer);
        sportAngebot.setLeitung("Leitung Text für " + kursnummer);
        sportAngebot.setOrt("Ort Text für " + kursnummer);
        int randomPreis = (firstTerminDate.getDayOfMonth() + 1) * 100;
        sportAngebot.setPreis(new SportAngebotPreis(randomPreis));
        sportAngebot.setBuchungsArt(ANGEBOT_TICKET_BUCHUNG);
        sportAngebot.setPasswortGesichert(false);

        SortedSet<SportTermin> sportTermine = newSportTermine(firstTerminDate);
        sportAngebot.setSportTermine(sportTermine);
        sportAngebot.setZeitraumStart(sportTermine.first().getTerminDate());
        sportAngebot.setZeitraumEnde(sportTermine.last().getTerminDate());
        return sportAngebot;
    }

    private static String newKursnummer(SportArt sportArt, LocalDateTime firstTerminDate) {
        String abbreviatedSportArtName = StringUtils.abbreviate(sportArt.getName(), "-", 4);
        String terminStr = String.format("%02d%02d", firstTerminDate.getDayOfMonth(), firstTerminDate.getMonthValue());
        return abbreviatedSportArtName + terminStr;
    }


    public static SortedSet<SportTermin> newSportTermine(LocalDateTime firstTerminDate) {
        SortedSet<SportTermin> sportTermine = new TreeSet<>();
        for (int i = 0; i < 4; i++) {
            LocalDateTime startZeit = firstTerminDate.plusWeeks(i);
            LocalDateTime endZeit = firstTerminDate.plusWeeks(i).plusMinutes(45);
            SportTermin sportTermin = new SportTermin(startZeit, endZeit);
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
        teilnehmer.setGender(TeilnehmerGender.FEMALE);

        teilnehmer.setTeilnehmerKategorie(TeilnehmerKategorie.MITARBEITER_FH);
        teilnehmer.setMitarbeiterNummer("MitarbeiterNummer Text");
        teilnehmer.setMatrikelnummer("Matrikelnummer Text");

        teilnehmer.setIban("DE92 24012 51235 213523");
        teilnehmer.setKontoInhaber(null);
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
        buchungsJob.setSportAngebot(sportAngebot);
        buchungsJob.setSportTermin(sportTermin);
        var sportBuchungsStrategie = new KonfigurierbareSportBuchungsStrategie();
        sportBuchungsStrategie.setDefaultDelay(Duration.ofMinutes(1));
        buchungsJob.setBuchungsWiederholungsStrategie(sportBuchungsStrategie);
        return buchungsJob;
    }


    private static Supplier<LocalDateTime> newRandomDateTimeSupplier(boolean generateFutureDateTime) {
        LocalDateTime now = LocalDate.now().atTime(LocalTime.of(10, 0));
        return () -> {
            int randomDayDelta = random.nextInt(99) + 1;
            int randomHour = random.nextInt(60 / 15) * 15;
            if (generateFutureDateTime) {
                return now.plusDays(randomDayDelta).plusHours(randomHour);
            } else {
                return now.minusDays(randomDayDelta).plusHours(randomHour);
            }
        };
    }

    public static SportBuchungsVersuch newBuchungsVersuch(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob.getBuchungsVersuche().size() < 4 || random.nextInt(100) > 90) {
            return SportBuchungsVersuch.newBuchungsVersuch(BUCHUNG_WARTELISTE);
        }
        int nextStatusIndex = random.nextInt(SportBuchungsVersuchStatus.values().length);
        SportBuchungsVersuchStatus status = SportBuchungsVersuchStatus.values()[nextStatusIndex];
        if (BUCHUNG_ERFOLGREICH.equals(status)) {
            SportBuchungsBestaetigung buchungsBestaetigung = new SportBuchungsBestaetigung();
            buchungsBestaetigung.setBuchungsJob(sportBuchungsJob);
            String randomBuchungsNummer = String.valueOf(random.nextInt(100_000));
            buchungsBestaetigung.setBuchungsNummer(randomBuchungsNummer);
            String kursnummer = sportBuchungsJob.getSportAngebot().getKursnummer();
            String buchungsBestaetigungUrl = String.format("https://example.com/sport/%s/confirmation/%s", //
                    kursnummer, randomBuchungsNummer);
            buchungsBestaetigung.setBuchungsBestaetigungUrl(buchungsBestaetigungUrl);
            return SportBuchungsVersuch.newErfolgreicherBuchungsVersuch(buchungsBestaetigung);
        } else {
            return SportBuchungsVersuch.newBuchungsVersuch(status);
        }
    }
}
