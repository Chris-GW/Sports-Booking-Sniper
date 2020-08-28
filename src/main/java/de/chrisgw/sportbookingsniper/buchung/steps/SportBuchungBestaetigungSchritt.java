package de.chrisgw.sportbookingsniper.buchung.steps;

import de.chrisgw.sportbookingsniper.buchung.SportBuchungsBestaetigung;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch.newErfolgreicherBuchungsVersuch;


@Slf4j
public class SportBuchungBestaetigungSchritt extends SeleniumSportBuchungsSchritt {

    public SportBuchungBestaetigungSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return extractBuchungsNummerFromUrl(driver.getCurrentUrl()) != null;
    }


    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.empty();
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        String buchungsNummer = extractBuchungsNummerFromUrl(driver.getCurrentUrl());
        log.info("buchungsBestaetigung document\n{}", driver.getPageSource());
        SportBuchungsBestaetigung sportBuchungsBestaetigung = new SportBuchungsBestaetigung();
        sportBuchungsBestaetigung.setBuchungsJob(buchungsJob);
        sportBuchungsBestaetigung.setBuchungsBestaetigungUrl(driver.getCurrentUrl());
        sportBuchungsBestaetigung.setBuchungsNummer(buchungsNummer);
        return newErfolgreicherBuchungsVersuch(sportBuchungsBestaetigung);
    }

    private String extractBuchungsNummerFromUrl(String currentUrl) {
        Pattern bestaetigungUrlPattern = Pattern.compile("Bestaetigung_(\\w+)");
        Matcher matcher = bestaetigungUrlPattern.matcher(currentUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("Could not extract buchungsNummer of url {} for pattern {}", currentUrl, bestaetigungUrlPattern);
        return null;
    }
}
