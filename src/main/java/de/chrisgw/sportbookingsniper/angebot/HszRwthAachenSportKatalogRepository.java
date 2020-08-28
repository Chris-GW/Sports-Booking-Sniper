package de.chrisgw.sportbookingsniper.angebot;


import de.chrisgw.sportbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import org.apache.commons.lang3.StringUtils;
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

import static de.chrisgw.sportbookingsniper.angebot.SportAngebot.SportAngebotBuchungsArt.*;
import static java.lang.Integer.parseInt;


@Slf4j
@Repository
public class HszRwthAachenSportKatalogRepository implements SportKatalogRepository {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final int REQUEST_TIMEOUT_MS = 8 * 1000;
    public static final String SPORT_KATALOG_URL = "http://buchung.hsz.rwth-aachen.de/angebote/aktueller_zeitraum/index.html";


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

            SportKatalog sportKatalog = new SportKatalog();
            sportKatalog.setKatalog(katalogName);
            sportKatalog.setZeitraumStart(zeitraumStart);
            sportKatalog.setZeitraumEnde(zeitraumEnde);
            sportKatalog.setAbrufzeitpunkt(LocalDateTime.now());
            return sportKatalog;
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

        SportArt sportArt = new SportArt(sportName, absoluteSportUrl.toString());
        sportArt.setSportAngebote(newLazySportAngebotLoader(sportArt));
        return sportArt;
    }

    @SuppressWarnings("unchecked")
    private Set<SportAngebot> newLazySportAngebotLoader(final SportArt sportArt) {
        return (Set<SportAngebot>) Enhancer.create(Set.class, (LazyLoader) () -> {
            log.trace("lazy load SportAngebote for {}", sportArt);
            return findSportAngeboteFor(sportArt);
        });
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
            throw new RuntimeException(String.format("expect zeitraum tb cell to match '%s' but was: '%s'", //
                    sportAngebotZeitraumPattern, zeitraumTd.text()));
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
        String leitung = doc.selectFirst("#bs_content .bs_verantw").text().trim();
        if (leitung.startsWith("verantwortlich: ")) {
            leitung = leitung.substring("verantwortlich: ".length());
        }

        List<SportAngebot> sportAngebote = new ArrayList<>();
        Elements sportAngebotBlocks = doc.select("#bs_content form .bs_angblock");
        for (Element sportAngebotBlock : sportAngebotBlocks) {
            Element kursBeschreibungDiv = sportAngebotBlock.selectFirst(".bs_kursbeschreibung").child(0);
            List<String> kursBeschreibungTexte = kursBeschreibungDiv.children().eachText();
            String beschreibung = StringUtils.capitalize(StringUtils.lowerCase(kursBeschreibungTexte.get(0)));
            String ort = kursBeschreibungTexte.get(1);

            Elements terminZeitfensterCells = sportAngebotBlock.select("table.bs_platz tr td.bs_sbuch");
            for (Element sportTerminCell : terminZeitfensterCells) {
                if (sportTerminCell.text().equalsIgnoreCase("keine Buchung") || !sportTerminCell.hasAttr("title")) {
                    continue;
                }
                SportAngebot sportAngebot = new SportAngebot();
                sportAngebot.setSportArt(sportArt);
                sportAngebot.setKursnummer(sportTerminCell.selectFirst("a").id());
                sportAngebot.setKursinfoUrl(null);
                sportAngebot.setBuchungsArt(EINZEL_PLATZ_BUCHUNG);
                sportAngebot.setPreis(new SportAngebotPreis());
                sportAngebot.setSportArt(sportArt);
                sportAngebot.setLeitung(leitung);
                sportAngebot.setOrt(ort);
                generateAllSportPlatzTermine(sportAngebot, sportTerminCell);
                String details = beschreibung + " - " + sportTerminCell.attr("title");
                sportAngebot.setDetails(details);
                if (isNewPlatzAngebotTimeSlot(sportAngebote, sportAngebot)) {
                    sportAngebote.add(sportAngebot);
                }
            }
        }
        sportAngebote.sort(Comparator.comparing(SportAngebot::getZeitraumStart));
        return new LinkedHashSet<>(sportAngebote);
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
        Pattern zeitfensterPattern = Pattern.compile("(\\d+:\\d+)\\s*-\\s*(\\d+:\\d+)(\\s+Uhr)?");
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

            SportTermin sportTermin = new SportTermin();
            sportTermin.setStartZeit(terminStart);
            sportTermin.setEndZeit(terminEnd);
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
        log.trace("readSportTermin {} from terminTr {}", sportTermin, terminTr);
        return sportTermin;
    }


    @SuppressWarnings("unchecked")
    private SortedSet<SportTermin> newTerminLazyLoader(final SportAngebot sportAngebot) {
        return (SortedSet<SportTermin>) Enhancer.create(SortedSet.class, (LazyLoader) () -> {
            log.debug("lazy load SportTermine for {}", sportAngebot);
            return findSportTermineFor(sportAngebot);
        });
    }

}
