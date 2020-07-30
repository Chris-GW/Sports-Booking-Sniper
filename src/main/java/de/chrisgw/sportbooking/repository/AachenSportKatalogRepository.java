package de.chrisgw.sportbooking.repository;


import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.model.SportTermin.SportTerminStatus;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.chrisgw.sportbooking.model.SportAngebotBuchungsArt.*;
import static java.lang.Integer.parseInt;


@Slf4j
@Repository
public class AachenSportKatalogRepository implements SportKatalogRepository {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String SPORT_KATALOG_URL = "http://buchung.hsz.rwth-aachen.de/angebote/aktueller_zeitraum/index.html";


    @Override
    public SportKatalog currentSportKatalog() {
        try {
            log.debug("loadSportArten() GET {}", SPORT_KATALOG_URL);
            Document doc = Jsoup.connect(SPORT_KATALOG_URL).timeout(8 * 1000).get();
            log.trace("loadSportArten() GET response document:\n{}", doc);
            SportKatalog sportKatalog = readSportKatalog(doc);

            Set<SportArt> sportArten = new HashSet<>(200);
            Elements sportLinkList = doc.select("div#bs_content dl.bs_menu dd a[href]");
            for (Element sportLink : sportLinkList) {
                sportArten.add(createSportArtOfLink(sportLink));
            }
            sportKatalog.setSportArten(sportArten);
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
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate zeitraumStart = LocalDate.parse(sportKatalogMatcher.group(2), dateFormatter);
            LocalDate zeitraumEnde = LocalDate.parse(sportKatalogMatcher.group(3), dateFormatter);

            SportKatalog sportKatalog = new SportKatalog();
            sportKatalog.setKatalog(katalogName);
            sportKatalog.setZeitraumStart(zeitraumStart);
            sportKatalog.setZeitraumEnde(zeitraumEnde);
            sportKatalog.setAbrufzeitpunkt(LocalDateTime.now());
            return sportKatalog;
        } else {
            String message = String.format("Expect top SportKatalog String to match '%s' but was: %s",
                    sportKatalogPattern, sportKatalogToprStr);
            throw new IllegalArgumentException(message);
        }
    }

    private SportArt createSportArtOfLink(Element sportLink) throws MalformedURLException {
        String sportName = sportLink.text();
        String sportUrlStr = sportLink.attr("href");
        URL absoluteSportUrl = new URL(new URL(SPORT_KATALOG_URL), sportUrlStr);

        SportArt sportArt = new SportArt(sportName, absoluteSportUrl.toString());
        sportArt.setSportAngebote(newLazySportAngebotLoader(sportArt));
        return sportArt;
    }

    @SuppressWarnings("unchecked")
    private Set<SportAngebot> newLazySportAngebotLoader(final SportArt sportArt) {
        return (Set<SportAngebot>) Enhancer.create(Set.class, (LazyLoader) () -> {
            log.debug("lazy load SportAngebote for {}", sportArt);
            return findSportAngeboteFor(sportArt);
        });
    }


    @Override
    public Set<SportAngebot> findSportAngeboteFor(SportArt sportArt) {
        try {
            log.debug("fetchSportAngebote() GET {}", sportArt.getUrl());
            Document doc = Jsoup.connect(sportArt.getUrl()).timeout(8 * 1000).get();
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
            SportAngebot sportAngebot = parseSportKursanbebotRow(sportTerminRow);
            sportAngebot.setSportArt(sportArt);
            sportAngebote.add(sportAngebot);
        }
        return sportAngebote;
    }

    private SportAngebot parseSportKursanbebotRow(Element tableRow) {
        SportAngebot sportAngebot = new SportAngebot();
        String kursnr = tableRow.child(0).text();
        String details = tableRow.child(1).text();
        SportAngebotBuchungsArt buchungsArt = EINMALIGES_TICKET_BUCHUNG;
        if (details.equalsIgnoreCase("Ein Termin")) {
            buchungsArt = EINZEL_TERMIN_BUCHUNG;
        }

        // only use first text
        String tag = tableRow.child(2).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);
        String zeit = tableRow.child(3).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);
        String ort = tableRow.child(4).textNodes().stream().findFirst().map(TextNode::getWholeText).orElse(null);

        Element zeitraumTd = tableRow.child(5);
        setReadZeitraum(sportAngebot, zeitraumTd);
        String kursinfoUrl = readKursinfoUrl(zeitraumTd);
        String leitung = tableRow.child(6).text();

        sportAngebot.setKursnummer(kursnr);
        sportAngebot.setDetails(details + " am " + tag + " von " + zeit);
        sportAngebot.setBuchungsArt(buchungsArt);
        sportAngebot.setOrt(ort);
        sportAngebot.setKursinfoUrl(kursinfoUrl);
        sportAngebot.setLeitung(leitung);
        sportAngebot.setPreis(readSportAngebotPreis(tableRow));
        sportAngebot.setSportTermine(newTerminLazyLoader(sportAngebot));
        return sportAngebot;
    }

    private void setReadZeitraum(SportAngebot sportAngebot, Element zeitraumTd) {
        Pattern sportAngebotZeitraumPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.-(\\d+)\\.(\\d+)\\.");
        Matcher zeitraumMatcher = sportAngebotZeitraumPattern.matcher(zeitraumTd.text());
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
        } else {
            log.warn("expect zeitraumStart to match '{}' but was: '{}'", sportAngebotZeitraumPattern,
                    zeitraumTd.text());
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
                throw new IllegalArgumentException("unexpected preis kategorie text: " + preisKategorieText);
            }
        }
        return new SportAngebotPreis(preisStudierende, preisMitarbeiter, preisExterne, prei1sAlumni);
    }

    private long parsePreis(String preisText) {
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
        String leitung = doc.selectFirst("#bs_content .bs_verantw").text().trim();

        Set<SportAngebot> sportAngebote = new LinkedHashSet<>();
        Elements sportAngebotBlocks = doc.select("#bs_content form .bs_angblock");
        for (Element sportAngebotBlock : sportAngebotBlocks) {
            List<TextNode> beschreibungTextNodes = sportAngebotBlock.selectFirst(".bs_kursbeschreibung").textNodes();
            String beschreibung = beschreibungTextNodes.get(0).getWholeText().trim();
            String ort = beschreibungTextNodes.get(1).text().trim();

            Elements terminZeitfensterCells = sportAngebotBlock.select("table.bs_platz tr td.bs_sbuch");
            for (Element sportTerminCell : terminZeitfensterCells) {
                if (sportTerminCell.text().equalsIgnoreCase("keine Buchung") || !sportTerminCell.hasAttr("title")) {
                    continue;
                }
                SportAngebot sportAngebot = parseSportPlatzZeitfensterAnbebot(sportArt, sportTerminCell);
                sportAngebot.setSportArt(sportArt);
                sportAngebot.setDetails(beschreibung);
                sportAngebot.setLeitung(leitung);
                sportAngebot.setOrt(ort);
            }
        }
        return sportAngebote;
    }

    private boolean hasAnyEinzelSportPlatzAngebote(Document doc) {
        return doc.select("#bs_content form .bs_angblock")
                .stream()
                .map(sportAngebotBlock -> sportAngebotBlock.select("table.bs_platz tr"))
                .anyMatch(sportTerminRow -> !sportTerminRow.isEmpty());
    }

    private SportAngebot parseSportPlatzZeitfensterAnbebot(SportArt sportArt, Element sportTerminCell) {
        SportAngebot sportAngebot = new SportAngebot();
        sportAngebot.setSportArt(sportArt);
        sportAngebot.setKursnummer(sportTerminCell.selectFirst("a").id());
        sportAngebot.setKursinfoUrl(null);
        sportAngebot.setBuchungsArt(EINZEL_PLATZ_BUCHUNG);
        sportAngebot.setPreis(new SportAngebotPreis());
        generateAllSportPlatzTermine(sportAngebot, sportTerminCell);
        return sportAngebot;
    }

    private void generateAllSportPlatzTermine(SportAngebot sportAngebot, Element sportTerminCell) {
        Pattern zeitfensterPattern = Pattern.compile("(\\d+:\\d+)\\s*-\\s*(\\d+:\\d+)(\\s+Uhr)?");
        String zeitfensterTitle = sportTerminCell.firstElementSibling().text();
        Matcher zeitfensterMatcher = zeitfensterPattern.matcher(zeitfensterTitle);
        if (!zeitfensterMatcher.matches()) {
            String message = String.format("expect SportTerminZeitfenster title attr to match '%s', but it was '%s')",
                    zeitfensterPattern, zeitfensterTitle);
            throw new RuntimeException(message);
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DayOfWeek zeitfensterWochentag = DayOfWeek.of(sportTerminCell.elementSiblingIndex() - 1);
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

            SportTermin sportTermin = new SportTermin();
            sportTermin.setStatus(SportTerminStatus.GESCHLOSSEN);
            sportTermin.setStartZeit(terminStart);
            sportTermin.setEndZeit(terminEnd);
            sportTermin.setBuchungsBeginn(terminStart.minusWeeks(1));
            sportAngebot.addSportTermin(sportTermin);
        }
        sportAngebot.setZeitraumStart(firstTerminDate);
        sportAngebot.setZeitraumEnde(terminStart.toLocalDate());
    }


    @Override
    public Set<SportTermin> findSportTermineFor(SportAngebot sportAngebot) {
        try {
            Set<SportTermin> sportTermine = new TreeSet<>();

            String kursinfoUrl = sportAngebot.getKursinfoUrl();
            log.debug("fetchSportTermine() GET {}", kursinfoUrl);
            Document doc = Jsoup.connect(kursinfoUrl).timeout(9 * 1000).get();
            log.trace("fetchSportTermine() GET response document:\n{}", doc);

            for (Element terminTr : doc.select("#main #bs_content table tbody tr")) {
                SportTermin sportTermin = readSportTermin(terminTr);
                sportTermin.setSportAngebot(sportAngebot);
                sportTermine.add(sportTermin);
            }
            return sportTermine;
        } catch (Exception e) {
            throw new RuntimeException("Could not fetchSportTermine()", e);
        }
    }

    private SportTermin readSportTermin(Element terminTr) {
        String datumStr = terminTr.child(1).text().trim();
        LocalDate terminDatum = LocalDate.parse(datumStr, DATE_FORMATTER);

        String[] uhrzeitStr = terminTr.child(2).text().split("-");
        String startZeitStr = uhrzeitStr[0].replace(".", ":").trim();
        String endZeitStr = uhrzeitStr[1].replace(".", ":").trim();
        LocalTime startZeit = LocalTime.parse(startZeitStr, TIME_FORMATTER);
        LocalTime endZeit = LocalTime.parse(endZeitStr, TIME_FORMATTER);

        SportTermin sportTermin = new SportTermin();
        sportTermin.setStartZeit(terminDatum.atTime(startZeit));
        sportTermin.setEndZeit(terminDatum.atTime(endZeit));
        sportTermin.setBuchungsBeginn(sportTermin.getStartZeit().minusWeeks(1));
        log.trace("readSportTermin {} from terminTr {}", sportTermin, terminTr);
        return sportTermin;
    }


    @SuppressWarnings("unchecked")
    private Set<SportTermin> newTerminLazyLoader(final SportAngebot sportAngebot) {
        return (Set<SportTermin>) Enhancer.create(Set.class, (LazyLoader) () -> {
            log.debug("lazy load SportTermine for {}", sportAngebot);
            return findSportTermineFor(sportAngebot);
        });
    }

}
