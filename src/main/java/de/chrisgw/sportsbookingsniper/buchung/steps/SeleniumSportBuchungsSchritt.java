package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch.SportBuchungsVersuchStatus.BUCHUNG_FEHLER;


@Log4j2
@RequiredArgsConstructor
public abstract class SeleniumSportBuchungsSchritt implements SportBuchungsSchritt {

    protected final WebDriver driver;


    public static SportBuchungsVersuch newVerbindlicherBuchungsVersuch(SportBuchungsJob buchungsJob) {
        WebDriver driver = null;
        try {
            driver = newWebDriver();
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            return newVerbindlicherBuchungsVersuch(driver, buchungsJob);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public static SportBuchungsVersuch newVerbindlicherBuchungsVersuch(WebDriver driver, SportBuchungsJob buchungsJob) {
        if (buchungsJob == null) {
            throw new NullPointerException("SportBuchungsJob must be non null");
        }
        try {
            log.debug("versucheVerbindlichZuBuchen() for {} with driver={}", buchungsJob, driver);
            return new GetSportAngebotWebpageSchritt(driver).executeBuchungsSchritt(buchungsJob);
        } catch (Exception e) {
            log.error("Could not book", e);
            return SportBuchungsVersuch.newBuchungsVersuch(BUCHUNG_FEHLER);
        }
    }

    private static WebDriver newWebDriver() {
        if (System.getProperty("webdriver.chrome.driver") != null) {
            return new ChromeDriver();
        }
        return new HtmlUnitDriver(true);
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        return possibleNextBuchungsSchritte(buchungsJob) //
                .filter(buchungsSchritt -> buchungsSchritt.isNextBuchungsSchritt(buchungsJob))
                .findAny()
                .map(nextBuchungsSchritt -> nextBuchungsSchritt.executeBuchungsSchritt(buchungsJob))
                .orElseGet(() -> {
                    log.warn("Could not find next BuchungsSchritt on page:\n{}", driver.getPageSource());
                    return SportBuchungsVersuch.newBuchungsVersuch(BUCHUNG_FEHLER);
                });
    }


    protected void switchToNewOpenWindow() {
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!currentWindowHandle.equals(windowHandle)) {
                log.trace("close WebDriver window {}", windowHandle);
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


    protected Optional<WebElement> findFormSubmitBtn(String submitBtnValue, String... submitBtnValues) {
        WebElement bsFormFooter = driver.findElement(By.id("bs_foot"));
        List<WebElement> bsFormButtons = bsFormFooter.findElements(By.tagName("input"));
        return bsFormButtons.stream().filter(hasValueAttribute(submitBtnValue, submitBtnValues)).findAny();
    }

    protected Predicate<WebElement> hasValueAttribute(String submitBtnValue, String[] submitBtnValues) {
        return webElement -> {
            String value = webElement.getAttribute("value");
            return submitBtnValue.equalsIgnoreCase(value) || (submitBtnValues != null && //
                    Arrays.stream(submitBtnValues).anyMatch(value::equalsIgnoreCase));
        };
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
