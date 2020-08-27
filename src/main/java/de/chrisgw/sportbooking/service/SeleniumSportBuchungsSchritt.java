package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.buchung.SportBuchungsJob;
import de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static de.chrisgw.sportbooking.model.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLGESCHLAGEN;


@RequiredArgsConstructor
public abstract class SeleniumSportBuchungsSchritt implements SportBuchungsSchritt {

    protected static final Logger log = LoggerFactory.getLogger(SeleniumSportBuchungsSchritt.class);
    protected final WebDriver driver;


    public static SportBuchungsVersuch newVerbindlicherBuchungsVersuch(SportBuchungsJob buchungsJob) {
        if (buchungsJob == null) {
            throw new NullPointerException("SportBuchungsJob must be non null");
        }
        WebDriver driver = newWebDriver();
        try {
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            log.debug("versucheVerbindlichZuBuchen() for {} with driver={}", buchungsJob, driver);
            return new GetSportAngebotWebpageSchritt(driver).executeBuchungsSchritt(buchungsJob);
        } catch (Exception e) {
            log.error("Could not book", e);
            return SportBuchungsVersuch.newBuchungsVersuch(BUCHUNG_FEHLGESCHLAGEN);
        } finally {
            driver.quit();
        }
    }

    private static WebDriver newWebDriver() {
        if (System.getProperty("webdriver.chrome.driver") != null) {
            return new ChromeDriver();
        } else {
            return new HtmlUnitDriver(true);
        }
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return possibleNextBuchungsSchritte() //
                .filter(buchungsSchritt -> buchungsSchritt.isNextBuchungsSchritt(buchungsJob))
                .findAny()
                .map(nextBuchungsSchritt -> nextBuchungsSchritt.executeBuchungsSchritt(buchungsJob))
                .orElse(null);
    }


    protected void switchToNewOpenWindow() {
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!currentWindowHandle.equals(windowHandle)) {
                driver.close();
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }

    protected Optional<LocalDateTime> readBuchungsBeginn(String buchungsBeginnText) {
        // "ab 01.09., 17:30"
        if (!buchungsBeginnText.matches("ab \\d{2}\\.\\d{2}\\., \\d{2}:\\d{2}")) {
            return Optional.empty();
        }
        DateTimeFormatter buchungsBeginnFormatter = DateTimeFormatter.ofPattern("'ab 'dd.MM.', 'HH:mm");
        MonthDay monthDay = MonthDay.parse(buchungsBeginnText, buchungsBeginnFormatter);
        LocalTime localTime = LocalTime.parse(buchungsBeginnText, buchungsBeginnFormatter);
        LocalDateTime buchungsBeginn = LocalDate.now().with(monthDay).atTime(localTime);
        if (buchungsBeginn.isAfter(LocalDateTime.now())) {
            buchungsBeginn = buchungsBeginn.plusYears(1);
        }
        log.trace("readBuchungsBeginn from .bs_btn_autostart {} -> {}", buchungsBeginnText, buchungsBeginn);
        return Optional.of(buchungsBeginn);
    }


    public static Optional<WebElement> findElement(SearchContext searchContext, By by) {
        List<WebElement> elements = searchContext.findElements(by);
        if (elements.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(elements.get(0));
    }

    public static Function<SearchContext, Optional<WebElement>> findElement(By by) {
        return searchContext -> findElement(searchContext, by);
    }


}
