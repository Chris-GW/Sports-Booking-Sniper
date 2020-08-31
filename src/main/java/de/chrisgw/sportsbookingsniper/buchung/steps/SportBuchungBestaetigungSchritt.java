package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.newErfolgreicherBuchungsVersuch;


@Slf4j
public class SportBuchungBestaetigungSchritt extends SeleniumSportBuchungsSchritt {

    private static final Pattern BESTAETIGUNG_URL_PATTERN = Pattern.compile("Bestaetigung_(\\w+)");

    public SportBuchungBestaetigungSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        String currentUrl = driver.getCurrentUrl();
        Matcher matcher = BESTAETIGUNG_URL_PATTERN.matcher(currentUrl);
        return matcher.find();
    }


    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.empty();
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        String buchungsNummer = extractBuchungsNummerFromUrl(driver.getCurrentUrl());
        SportBuchungsBestaetigung buchungsBestaetigung = new SportBuchungsBestaetigung();
        buchungsBestaetigung.setBuchungsJob(buchungsJob);
        buchungsBestaetigung.setBuchungsBestaetigungUrl(driver.getCurrentUrl());
        buchungsBestaetigung.setBuchungsNummer(buchungsNummer);
        log.info("{}: buchungsBestaetigung={} with buchungsNummer={}", buchungsJob, buchungsBestaetigung,
                buchungsNummer);
        if (log.isTraceEnabled()) {
            log.trace("buchungsBestaetigung document {}\n{}", driver.getCurrentUrl(), driver.getPageSource());
        }
        return newErfolgreicherBuchungsVersuch(buchungsBestaetigung);
    }

    private String extractBuchungsNummerFromUrl(String currentUrl) {
        Matcher matcher = BESTAETIGUNG_URL_PATTERN.matcher(currentUrl);
        if (!matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException(String.format("Could not extract buchungsNummer of url %s for pattern %s", //
                currentUrl, BESTAETIGUNG_URL_PATTERN));
    }

}
