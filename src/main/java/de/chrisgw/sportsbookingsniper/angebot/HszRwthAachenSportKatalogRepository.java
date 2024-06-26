package de.chrisgw.sportsbookingsniper.angebot;


import de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.ANGEBOT_TICKET_BUCHUNG;
import static de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.EINZEL_PLATZ_BUCHUNG;
import static de.chrisgw.sportsbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.EINZEL_TERMIN_BUCHUNG;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;


@Log4j2
public class HszRwthAachenSportKatalogRepository implements SportKatalogRepository {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final int REQUEST_TIMEOUT_MS = 8 * 1000;
    public static final String SPORT_KATALOG_URL = "https://buchung.hsz.rwth-aachen.de/angebote/aktueller_zeitraum/index.html";


    @Override
    public SportKatalog findCurrentSportKatalog() {
        try {
            log.debug("loadSportArten() GET {}", SPORT_KATALOG_URL);
            Document doc = Jsoup.connect(SPORT_KATALOG_URL).timeout(REQUEST_TIMEOUT_MS).get();
            log.trace("loadSportArten() GET response document:\n{}", doc);
            SportKatalog sportKatalog = readSportKatalog(doc);

            Elements sportLinkList = doc.select("div#bs_content dl.bs_menu dd a[href]");
            for (Element sportLink : sportLinkList) {
                SportArt sportArt = createSportArtOfLink(sportLink);
                sportKatalog.addSportArt(sportArt);
            }
            return sportKatalog;
        } catch (Exception e) {
            throw new RuntimeException("Could not loadSportArten", e);
        }
    }

    private SportKatalog readSportKatalog(Document doc) {
        // "Sommersemester 2019 (01.04.2019-06.10.2019)"
        // "Wintersemester 2019/20 (07.10.2019-05.04.2020)"
        // "Übergangsprogramm 2020 (18.05.2020-11.10.2020)"
        Pattern sportKatalogPattern = Pattern.compile(
                "(.+)\\s+\\((\\d+\\.\\d+\\.\\d+)\\s*-\\s*(\\d+\\.\\d+\\.\\d+)\\)");
        String sportKatalogToprStr = doc.getElementById("bs_top").text();
        Matcher sportKatalogMatcher = sportKatalogPattern.matcher(sportKatalogToprStr);
        if (sportKatalogMatcher.matches()) {
            String katalogName = sportKatalogMatcher.group(1);
            LocalDate zeitraumStart = LocalDate.parse(sportKatalogMatcher.group(2), DATE_FORMATTER);
            LocalDate zeitraumEnde = LocalDate.parse(sportKatalogMatcher.group(3), DATE_FORMATTER);
            return SportKatalog.builder()
                    .katalog(katalogName)
                    .zeitraumStart(zeitraumStart)
                    .zeitraumEnde(zeitraumEnde)
                    .build();
        } else {
            String message = String.format("Expect top SportKatalog String to match Pattern '%s' but was: '%s'",
                    sportKatalogPattern, sportKatalogToprStr);
            throw new IllegalArgumentException(message);
        }
    }

    private SportArt createSportArtOfLink(Element sportLink) throws MalformedURLException {
        String sportName = sportLink.text();
        String sportUrlStr = sportLink.attr("href");
        URL absoluteSportUrl = new URL(new URL(SPORT_KATALOG_URL), sportUrlStr);

        SportArt sportArt = new SportArt(sportName, absoluteSportUrl.toString()) {

            @Override
            public Set<SportAngebot> getSportAngebote() {
                if (super.getSportAngebote() == null) {
                    synchronized (this) {
                        if (super.getSportAngebote() == null) {
                            log.trace("lazy load SportAngebote for {}", this);
                            setSportAngebote(findSportAngeboteFor(this));
                        }
                    }
                }
                return super.getSportAngebote();
            }
        };
        sportArt.setSportAngebote(null); // lazy init
        return sportArt;
    }


    @Override
    public Set<SportAngebot> findSportAngeboteFor(SportArt sportArt) {
        try {
            log.debug("fetchSportAngebote() GET {}", sportArt.getUrl());
            Document doc = Jsoup.connect(sportArt.getUrl()).timeout(REQUEST_TIMEOUT_MS).get();
            log.trace("fetchSportAngebote() GET response document:\n{}", doc);

            Set<SportAngebot> sportAngebote = new TreeSet<>();
            sportAngebote.addAll(parseSportKursangebotTabelle(sportArt, doc));
            sportAngebote.addAll(parseSportPlatzAngebotTabelle(sportArt, doc));
            return sportAngebote;
        } catch (Exception e) {
            throw new RuntimeException("Could not fetchSportAngebote", e);
        }
    }


    private Set<SportAngebot> parseSportKursangebotTabelle(SportArt sportArt, Document doc) {
        Set<SportAngebot> sportAngebote = new TreeSet<>();
        Elements kursRows = doc.select("#bs_content table.bs_kurse tbody tr");
        for (Element sportTerminRow : kursRows) {
            SportAngebot sportAngebot = parseSportKursangebotRow(sportTerminRow);
            sportAngebot.setSportArt(sportArt);
            sportAngebote.add(sportAngebot);
        }
        return sportAngebote;
    }

    private SportAngebot parseSportKursangebotRow(Element tableRow) {
        SportAngebot sportAngebot = new SportAngebot() {

            @Override
            public SortedSet<SportTermin> getSportTermine() {
                if (super.getSportTermine() == null) {
                    synchronized (this) {
                        if (super.getSportTermine() == null) {
                            log.debug("lazy load SportTermine for {}", this);
                            setSportTermine(findSportTermineFor(this));
                        }
                    }
                }
                return super.getSportTermine();
            }
        };
        String kursnr = tableRow.child(0).text();
        String details = tableRow.child(1).text();
        SportAngebotBuchungsArt buchungsArt = ANGEBOT_TICKET_BUCHUNG;
        if (details.equalsIgnoreCase("Ein Termin")) {
            buchungsArt = EINZEL_TERMIN_BUCHUNG;
        }

        // only use first text
        String tag = tableRow.child(2).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);
        String zeit = tableRow.child(3).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);
        String ort = tableRow.child(4).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);

        Element zeitraumTd = tableRow.child(5);
        String kursinfoUrl = readKursinfoUrl(zeitraumTd);
        String leitung = tableRow.child(6).text();
        SportAngebotPreis preis = readSportAngebotPreis(tableRow);

        sportAngebot.setKursnummer(kursnr);
        sportAngebot.setDetails(details + " am " + tag + " von " + zeit);
        sportAngebot.setBuchungsArt(buchungsArt);
        sportAngebot.setOrt(ort);
        setReadZeitraum(sportAngebot, zeitraumTd);
        sportAngebot.setKursinfoUrl(kursinfoUrl);
        sportAngebot.setLeitung(leitung);
        sportAngebot.setPreis(preis);
        sportAngebot.setPasswortGesichert(isPasswortGesichert(tableRow));
        sportAngebot.setSportTermine(null); // lazy init
        return sportAngebot;
    }


    private boolean isPasswortGesichert(Element tableRow) {
        Element buchenBtn = tableRow.selectFirst("input.bs_btn_buchen");
        return buchenBtn != null && "booking - password protected".equalsIgnoreCase(buchenBtn.attr("title"));
    }


    private void setReadZeitraum(SportAngebot sportAngebot, Element zeitraumTd) {
        Pattern sportAngebotZeitraumPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.-(\\d+)\\.(\\d+)\\.");
        Pattern sportAngebotEinzelterminPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.");
        Matcher zeitraumMatcher = sportAngebotZeitraumPattern.matcher(zeitraumTd.text());
        Matcher einzelTerminMatcher = sportAngebotEinzelterminPattern.matcher(zeitraumTd.text());
        if (zeitraumMatcher.matches()) {
            int year = LocalDate.now().getYear();
            int startDay = parseInt(zeitraumMatcher.group(1));
            int startMonth = parseInt(zeitraumMatcher.group(2));
            LocalDate zeitraumStart = LocalDate.of(year, startMonth, startDay);

            int endDay = parseInt(zeitraumMatcher.group(3));
            int endMonth = parseInt(zeitraumMatcher.group(4));
            LocalDate zeitraumEnde = LocalDate.of(year, endMonth, endDay);
            if (zeitraumEnde.isBefore(zeitraumStart)) {
                // so this zeitraum is in wintersemester and wraps around new year
                zeitraumStart = zeitraumStart.minusYears(1);
            }
            sportAngebot.setZeitraumStart(zeitraumStart);
            sportAngebot.setZeitraumEnde(zeitraumEnde);
        } else if (einzelTerminMatcher.matches()) {
            int year = LocalDate.now().getYear();
            int startDay = parseInt(einzelTerminMatcher.group(1));
            int startMonth = parseInt(einzelTerminMatcher.group(2));
            LocalDate einzelTerminDate = LocalDate.of(year, startMonth, startDay);
            sportAngebot.setZeitraumStart(einzelTerminDate);
            sportAngebot.setZeitraumEnde(einzelTerminDate);
        } else {
            throw new RuntimeException(String.format("expect zeitraum tb cell to match '%s' but was: '%s'", //
                    sportAngebotEinzelterminPattern, zeitraumTd.text()));
        }
    }

    private String readKursinfoUrl(Element zeitraumTd) {
        return zeitraumTd.select("a").first().attr("abs:href");
    }

    private SportAngebotPreis readSportAngebotPreis(Element tableRow) {
        Elements preisForKategorie = tableRow.select(".bs_spreis .bs_tt1, .bs_spreis .bs_tt2");

        long preisStudierende = 0;
        long preisMitarbeiter = 0;
        long preisExterne = 0;
        long prei1sAlumni = 0;
        for (int i = 0; i < preisForKategorie.size() - 1; i += 2) {
            String preisText = preisForKategorie.get(i).text();
            String preisKategorieText = preisForKategorie.get(i + 1).text();
            long preis = parsePreis(preisText);

            switch (preisKategorieText) {
            case "für Studierende":
                preisStudierende = preis;
                break;
            case "für Beschäftigte":
                preisMitarbeiter = preis;
                break;
            case "für Externe":
                preisExterne = preis;
                break;
            case "für Alumni":
                prei1sAlumni = preis;
                break;
            default:
                log.warn("unexpected preis kategorie text: {}", preisKategorieText);
            }
        }
        SportAngebotPreis preis = new SportAngebotPreis(preisStudierende, preisMitarbeiter, preisExterne, prei1sAlumni);
        log.trace("readSportAngebotPreis for row [{}] '{}' as SportAngebotPreis = {}", //
                tableRow.siblingIndex(), preisForKategorie.text(), prei1sAlumni);
        return preis;
    }

    private long parsePreis(String preisText) {
        /* 12 EUR für Studierende
           12 EUR für Beschäftigte
           12 EUR für Externe
           12 EUR für Alumni */
        Pattern preisPattern = Pattern.compile("(?<preis>\\d+(,\\d+)?)\\s+EUR");
        Matcher preisMatcher = preisPattern.matcher(preisText);
        if (preisMatcher.matches()) {
            String preisStr = preisMatcher.group("preis").replace(",", ".");
            return Math.round(Double.parseDouble(preisStr) * 100);
        } else {
            throw new IllegalArgumentException("expect preis to match '" + preisMatcher + "', but was: " + preisText);
        }
    }


    private Set<SportAngebot> parseSportPlatzAngebotTabelle(SportArt sportArt, Document doc) {
        if (!hasAnyEinzelSportPlatzAngebote(doc)) {
            return Collections.emptySet();
        }
        String anbeotName = doc.selectFirst("#bs_content .bs_head").text().trim();
        String leitung = readLeitung(doc);

        List<SportAngebot> sportAngebote = new ArrayList<>();
        Elements sportAngebotBlocks = doc.select("#bs_content form .bs_angblock");
        for (Element sportAngebotBlock : sportAngebotBlocks) {
            String beschreibung = readBeschreibung(sportAngebotBlock);
            String ort = readOrt(sportAngebotBlock);

            Elements terminZeitfensterCells = sportAngebotBlock.select("table.bs_platz tr td.bs_sbuch");
            for (Element sportTerminCell : terminZeitfensterCells) {
                if (sportTerminCell.text().equalsIgnoreCase("keine Buchung") || !sportTerminCell.hasAttr("title")) {
                    continue;
                }
                String kursnummer = sportTerminCell.selectFirst("a").id();
                String details = beschreibung + " - " + sportTerminCell.attr("title");
                SportAngebot sportAngebot = new SportAngebot();
                sportAngebot.setSportArt(sportArt);
                sportAngebot.setKursnummer(kursnummer);
                sportAngebot.setKursinfoUrl(null);
                sportAngebot.setBuchungsArt(EINZEL_PLATZ_BUCHUNG);
                sportAngebot.setPreis(new SportAngebotPreis());
                sportAngebot.setLeitung(leitung);
                sportAngebot.setOrt(ort);
                sportAngebot.setDetails(details);
                generateAllSportPlatzTermine(sportAngebot, sportTerminCell);
                if (isNewPlatzAngebotTimeSlot(sportAngebote, sportAngebot)) {
                    sportAngebote.add(sportAngebot);
                }
            }
        }
        sportAngebote.sort(Comparator.comparing(SportAngebot::getZeitraumStart));
        return new LinkedHashSet<>(sportAngebote);
    }


    private String readLeitung(Document doc) {
        String leitung = doc.selectFirst("#bs_content .bs_verantw").text().trim();
        if (leitung.startsWith("verantwortlich: ")) {
            leitung = leitung.substring("verantwortlich: ".length());
        }
        return leitung;
    }

    private String readBeschreibung(Element sportAngebotBlock) {
        Element kursBeschreibungDiv = sportAngebotBlock.selectFirst(".bs_kursbeschreibung");
        List<String> strongTexts = kursBeschreibungDiv.select("strong").eachText();
        if (strongTexts.isEmpty()) {
            Element bsHeadElement = sportAngebotBlock.ownerDocument().selectFirst("#bs_content .bs_head");
            return bsHeadElement.text();
        } else {
            String firstStrongText = strongTexts.get(0);
            return capitalize(lowerCase(firstStrongText));
        }
    }

    private String readOrt(Element sportAngebotBlock) {
        Element kursbeschreibungDiv = sportAngebotBlock.selectFirst(".bs_kursbeschreibung");
        List<String> strongTexts = kursbeschreibungDiv.select("strong").eachText();
        if (strongTexts.size() >= 2) {
            String lastStrongText = strongTexts.get(strongTexts.size() - 1);
            return trim(lastStrongText);
        } else {
            return null;
        }
    }


    private boolean isNewPlatzAngebotTimeSlot(Collection<SportAngebot> sportAngebote, SportAngebot sportAngebot) {
        SportTermin firstTermin = sportAngebot.getSportTermine().first();
        return sportAngebote.stream()
                .map(otherSportAngebot -> otherSportAngebot.getSportTermine().first())
                .noneMatch(firstTermin::isSameTimeSlotAs);
    }

    private boolean hasAnyEinzelSportPlatzAngebote(Document doc) {
        return doc.select("#bs_content form .bs_angblock")
                .stream()
                .map(sportAngebotBlock -> sportAngebotBlock.select("table.bs_platz tr"))
                .anyMatch(sportTerminRow -> !sportTerminRow.isEmpty());
    }

    private void generateAllSportPlatzTermine(SportAngebot sportAngebot, Element sportTerminCell) {
        Pattern zeitfensterPattern = Pattern.compile("(\\d+[:.]\\d+)\\s*-\\s*(\\d+[:.]\\d+)(\\s+Uhr)?");
        String zeitfensterTitle = sportTerminCell.firstElementSibling().text();
        Matcher zeitfensterMatcher = zeitfensterPattern.matcher(zeitfensterTitle);
        if (!zeitfensterMatcher.matches()) {
            String message = String.format("expect SportTerminZeitfenster title attr to match '%s', but it was '%s')",
                    zeitfensterPattern, zeitfensterTitle);
            throw new RuntimeException(message);
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DayOfWeek zeitfensterWochentag = DayOfWeek.of(sportTerminCell.elementSiblingIndex());
        LocalTime zeitfensterStartZeit = LocalTime.parse(zeitfensterMatcher.group(1), timeFormatter);
        LocalTime zeitfensterEndZeit = LocalTime.parse(zeitfensterMatcher.group(2), timeFormatter);
        Duration zeitfensterDuration = Duration.between(zeitfensterStartZeit, zeitfensterEndZeit);

        SportKatalog sportKatalog = sportAngebot.getSportArt().getSportKatalog();
        LocalDate sportKatalogStart = sportKatalog.getZeitraumStart();
        LocalDate sportKatalogEnde = sportKatalog.getZeitraumEnde();
        LocalDate firstTerminDate = sportKatalogStart.with(TemporalAdjusters.nextOrSame(zeitfensterWochentag));

        LocalDateTime terminStart = firstTerminDate.atTime(zeitfensterStartZeit);
        LocalDateTime terminEnd = terminStart.plus(zeitfensterDuration);
        while (!terminStart.toLocalDate().isAfter(sportKatalogEnde)) {
            terminStart = terminStart.plusWeeks(1);
            terminEnd = terminEnd.plusWeeks(1);

            SportTermin sportTermin = new SportTermin(terminStart, terminEnd);
            sportAngebot.addSportTermin(sportTermin);
        }
        sportAngebot.setZeitraumStart(firstTerminDate);
        sportAngebot.setZeitraumEnde(terminStart.toLocalDate());
    }


    @Override
    public SortedSet<SportTermin> findSportTermineFor(SportAngebot sportAngebot) {
        try {
            SortedSet<SportTermin> sportTermine = new TreeSet<>();

            String kursinfoUrl = sportAngebot.getKursinfoUrl();
            log.debug("fetchSportTermine() GET {}", kursinfoUrl);
            Document doc = Jsoup.connect(kursinfoUrl).timeout(REQUEST_TIMEOUT_MS).get();
            log.trace("fetchSportTermine() GET response document:\n{}", doc);

            for (Element terminTr : doc.select("#main #bs_content table tbody tr")) {
                SportTermin sportTermin = readSportTermin(terminTr);
                sportTermine.add(sportTermin);
            }
            return sportTermine;
        } catch (Exception e) {
            throw new RuntimeException("Could not fetchSportTermine()", e);
        }
    }

    private SportTermin readSportTermin(Element terminTr) {
        log.traceEntry(() -> terminTr);
        String datumStr = terminTr.child(1).text().trim();
        log.trace("datum cell[1] '{}'", datumStr);
        LocalDate terminDatum = LocalDate.parse(datumStr, DATE_FORMATTER);
        log.trace("datum cell[1] '{}' parsed terminDatum: {}", datumStr, terminDatum);

        String uhrzeitStr = terminTr.child(2).text();
        LocalDateTime startZeit = terminDatum.atTime(0, 0);
        LocalDateTime endZeit = terminDatum.atTime(0, 0);
        log.trace("uhrzeit cell[2] '{}'", uhrzeitStr);
        if (uhrzeitStr.matches("\\d{1,2}[:.]\\d{1,2}\\s*-\\s*\\d{1,2}[:.]\\d{1,2}")) {
            String[] splitUhrzeitStr = uhrzeitStr.split("-");
            String startZeitStr = splitUhrzeitStr[0].replace(".", ":").trim();
            String endZeitStr = splitUhrzeitStr[1].replace(".", ":").trim();
            startZeit = LocalTime.parse(startZeitStr, TIME_FORMATTER).atDate(terminDatum);
            endZeit = LocalTime.parse(endZeitStr, TIME_FORMATTER).atDate(terminDatum);
            log.trace("uhrzeit cell[2] '{}' parsed startTime {} and endTime {}", //
                    uhrzeitStr, startZeit, endZeit);
        } else {
            log.warn("uhrzeit cell[2] '{}' from terminTr: {}", uhrzeitStr, terminTr);
        }
        SportTermin sportTermin = new SportTermin(startZeit, endZeit);
        log.traceExit(sportTermin);
        return sportTermin;
    }


}
