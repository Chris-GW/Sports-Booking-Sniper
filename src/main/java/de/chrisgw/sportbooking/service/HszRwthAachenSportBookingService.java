package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class HszRwthAachenSportBookingService implements SportBookingService {


    @Override
    public SportBuchungsBestaetigung versucheVerbindlichZuBuchen(SportBuchungsJob sportBuchungsJob) {
        if (sportBuchungsJob == null) {
            throw new NullPointerException("SportBuchungsJob must be non null");
        }
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();

        //        WebDriver driver = new ChromeDriver();
        WebDriver driver = new HtmlUnitDriver(true);
        try {
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            log.debug("verbindlichBuchen() for {} with driver={}", sportBuchungsJob, driver);
            // navigate to SportAngebot page
            SportAngebot sportAngebot = sportTermin.getSportAngebot();
            SportArt sportArt = sportAngebot.getSportArt();
            driver.get(sportArt.getUrl());
            boolean isBuchbarerSportTermin =
                    clickOntoSportAngebot(driver, sportTermin) && clickOntoSportTermin(driver, sportTermin);
            if (!isBuchbarerSportTermin) {
                return null;
            }
            fillPersonenAngaben(driver, sportBuchungsJob);
            // submit final booking form
            tryToSubmitForm(driver);

            String buchungsNummer = extractBuchungsNummerFromUrl(driver.getCurrentUrl());
            if (buchungsNummer != null) {
                log.info("buchungsBestaetigung document\n{}", driver.getPageSource());
                SportBuchungsBestaetigung sportBuchungsBestaetigung = new SportBuchungsBestaetigung();
                sportBuchungsBestaetigung.setBuchungsJob(sportBuchungsJob);
                sportBuchungsBestaetigung.setBuchungsBestaetigungUrl(driver.getCurrentUrl());
                sportBuchungsBestaetigung.setBuchungsNummer(buchungsNummer);
                // byte[] buchungsBestaetigungPdf = readBuchungsBestaetigungAsPdf(driver.getPageSource());
                // sportBuchungsBestaetigung.setBuchungsBestaetigung(buchungsBestaetigungPdf);
                return sportBuchungsBestaetigung;
            }

        } catch (Exception e) {
            log.error("Could not book", e);
        } finally {
            driver.quit();
        }
        return null;
    }

    private boolean clickOntoSportAngebot(WebDriver driver, SportTermin sportTermin) {
        SportAngebot sportAngebot = sportTermin.getSportAngebot();
        Optional<WebElement> angebotRow = findAngebotRow(driver, sportAngebot);
        Optional<WebElement> angebotBuchenCell = angebotRow.map(this::findAngebotBuchenCell);
        boolean hasAngebotBuchenBtn = angebotBuchenCell.map(this::hasAngebotBuchenBtn).orElse(false);
        if (hasAngebotBuchenBtn) {
            WebElement angebotBuchenBtn = angebotBuchenCell.get().findElement(By.className("bs_btn_buchen"));
            log.debug("clickOntoSportAngebot {}", angebotBuchenBtn.getAttribute("name"));
            angebotBuchenBtn.click();
            switchToNewOpenWindow(driver);
        } else {
            Optional<String> wartezeit = angebotBuchenCell.map(
                    webElement -> webElement.findElement(By.className("bs_btn_autostart"))).map(WebElement::getText);
            log.debug("clickOntoSportAngebot could not click onto {}", wartezeit.orElse("td.bs_sbuch"));
            if (wartezeit.isPresent()) {
                String angebotBuchenStr = wartezeit.get().substring("ab ".length()).trim();
                DateTimeFormatter buchungsBeginnFormatter = DateTimeFormatter.ofPattern("dd.MM., HH:mm");
                MonthDay monthDay = MonthDay.parse(angebotBuchenStr, buchungsBeginnFormatter);
                LocalTime localTime = LocalTime.parse(angebotBuchenStr, buchungsBeginnFormatter);
                LocalDateTime buchungsBeginn = LocalDate.now().with(monthDay).atTime(localTime);
                sportTermin.setBuchungsBeginn(buchungsBeginn);
            }
        }
        return hasAngebotBuchenBtn;
    }

    private Optional<WebElement> findAngebotRow(WebDriver driver, SportAngebot sportAngebot) {
        String kursnummer = sportAngebot.getKursnummer();
        return driver.findElements(By.cssSelector("#bs_content table.bs_kurse tbody tr"))
                .stream()
                .filter(angebotRow -> {
                    WebElement kursnummerTd = angebotRow.findElement(By.className("bs_sknr"));
                    return kursnummer.equals(kursnummerTd.getAttribute("textContent"));
                })
                .findAny();
    }

    private WebElement findAngebotBuchenCell(WebElement angebotRow) {
        return angebotRow.findElement(By.className("bs_sbuch"));
    }

    private boolean hasAngebotBuchenBtn(WebElement angebotBuchenCell) {
        List<WebElement> inputElements = angebotBuchenCell.findElements(By.className("bs_btn_buchen"));
        return !inputElements.isEmpty();
    }

    private void switchToNewOpenWindow(WebDriver driver) {
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!currentWindowHandle.equals(windowHandle)) {
                driver.close();
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }


    private boolean clickOntoSportTermin(WebDriver driver, SportTermin sportTermin) {
        Optional<WebElement> terminInput = findTerminInput(driver, sportTermin);
        if (!terminInput.isPresent()) {
            return false;
        }
        terminInput.ifPresent(WebElement::click);
        List<WebElement> footerSubmitBtns = driver.findElement(By.id("bs_foot")).findElements(By.tagName("input"));
        Optional<WebElement> submitBtn = footerSubmitBtns.stream()
                .filter(webElement -> webElement.getAttribute("type").equalsIgnoreCase("submit"))
                .findAny();
        submitBtn.ifPresent(WebElement::click);
        return submitBtn.isPresent();
    }

    private Optional<WebElement> findTerminInput(WebDriver driver, SportTermin sportTermin) {
        String formattedTerminDate = DateTimeFormatter.ISO_DATE.format(sportTermin.getTerminDate());
        WebElement mainBuchungsForm = driver.findElement(By.id("bs_form_main"));
        for (WebElement terminInput : mainBuchungsForm.findElements(By.tagName("input"))) {
            String name = terminInput.getAttribute("name");
            String value = terminInput.getAttribute("value");
            if (name.equalsIgnoreCase("Termin") && value.equals(formattedTerminDate)) {
                return Optional.of(terminInput);
            }
        }
        return Optional.empty();
    }


    private void fillPersonenAngaben(WebDriver driver, SportBuchungsJob sportBuchungsJob) {
        TeilnehmerAngaben teilnehmerAngaben = sportBuchungsJob.getTeilnehmerAngaben();
        log.debug("fill form with given PersonenAngaben {}", teilnehmerAngaben);
        WebElement personenAngabenForm = driver.findElement(By.name("bsform"));
        selectGenderRadio(personenAngabenForm, teilnehmerAngaben);

        personenAngabenForm.findElement(By.name("vorname")).clear();
        personenAngabenForm.findElement(By.name("vorname")).sendKeys(teilnehmerAngaben.getVorname());
        personenAngabenForm.findElement(By.name("name")).clear();
        personenAngabenForm.findElement(By.name("name")).sendKeys(teilnehmerAngaben.getNachname());
        personenAngabenForm.findElement(By.name("email")).clear();
        personenAngabenForm.findElement(By.name("email")).sendKeys(teilnehmerAngaben.getEmail());
        if (teilnehmerAngaben.getTelefon() != null) {
            personenAngabenForm.findElement(By.name("telefon")).sendKeys(teilnehmerAngaben.getTelefon());
        }

        personenAngabenForm.findElement(By.name("strasse")).clear();
        personenAngabenForm.findElement(By.name("strasse")).sendKeys(teilnehmerAngaben.getStreet());
        personenAngabenForm.findElement(By.name("ort")).clear();
        personenAngabenForm.findElement(By.name("ort")).sendKeys(teilnehmerAngaben.getOrt());

        selectPersonenKategorie(personenAngabenForm, teilnehmerAngaben);

        if (sportBuchungsJob.getSportTermin().getSportAngebot().isPaymentRequierd()) {
            personenAngabenForm.findElement(By.name("iban")).clear();
            personenAngabenForm.findElement(By.name("iban")).sendKeys(teilnehmerAngaben.getIban());
            if (teilnehmerAngaben.getKontoInhaber() != null) {
                personenAngabenForm.findElement(By.name("kontoinh")).clear();
                personenAngabenForm.findElement(By.name("kontoinh")).sendKeys(teilnehmerAngaben.getKontoInhaber());
            }
        }

        personenAngabenForm.findElement(By.name("tnbed")).click();
    }

    private Optional<WebElement> selectGenderRadio(WebElement personenAngabenForm,
            TeilnehmerAngaben teilnehmerAngaben) {
        String genderShortName = teilnehmerAngaben.getGender().getShortName();
        List<WebElement> genderInputs = personenAngabenForm.findElements(By.cssSelector("input[name='sex']"));
        for (WebElement genderInput : genderInputs) {
            String value = genderInput.getAttribute("value");
            if (value.equalsIgnoreCase(genderShortName)) {
                genderInput.click();
                return Optional.of(genderInput);
            }
        }
        return Optional.empty();
    }

    private void selectPersonenKategorie(SearchContext personenAngabenForm, TeilnehmerAngaben teilnehmerAngaben) {
        TeilnehmerKategorie teilnehmerKategorie = teilnehmerAngaben.getTeilnehmerKategorie();
        new Select(personenAngabenForm.findElement(By.name("statusorig"))).selectByValue(
                teilnehmerKategorie.getValue());
        switch (teilnehmerKategorie) {
        case STUDENT_FH:
        case STUDENT_RWTH:
        case STUDENT_NRW:
        case STUDENT_ANDERE_HOCHSCHULE:
            personenAngabenForm.findElement(By.name("matnr")).clear();
            personenAngabenForm.findElement(By.name("matnr")).sendKeys(teilnehmerAngaben.getMatrikelnummer());
            break;
        case MITARBEITER_FH:
        case MITARBEITER_RWTH:
        case MITARBEITER_KLINIKUM:
            personenAngabenForm.findElement(By.name("mitnr")).clear();
            personenAngabenForm.findElement(By.name("mitnr")).sendKeys(teilnehmerAngaben.getMitarbeiterNummer());
            break;
        case SCHUELER:
        case AZUBI_RWTH_UKA:
        case EXTERN:
            // no special data needed to enter
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown PersonenKategorie " + teilnehmerAngaben.getTeilnehmerKategorie());
        }
    }


    private void tryToSubmitForm(WebDriver driver) {
        int maxSubmitTries = 6;
        for (int i = 1; i <= maxSubmitTries; i++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.warn("tryToSubmitForm() sleep interrupted", e);
                break;
            }
            List<WebElement> bsForm = driver.findElements(By.name("bsform"));
            if (bsForm.size() <= 0) {
                return; // can't find bsForm, so we are done
            }
            WebElement bsFormFooter = driver.findElement(By.id("bs_foot"));
            validatePersonenAngaben(bsForm.get(0));

            List<WebElement> bsFormButtons = bsFormFooter.findElements(By.tagName("input"));
            List<String> continueButtonValues = Arrays.asList( //
                    "weiter zur Buchung", "verbindlich buchen", "kostenpflichtig buchen");
            for (WebElement bsFormButton : bsFormButtons) {
                String value = bsFormButton.getAttribute("value");
                if (continueButtonValues.contains(value)) {
                    log.debug("tryToSubmitForm() phase={}, try={}", value, i);
                    log.debug(Jsoup.parse(driver.getPageSource()).toString());
                    bsFormButton.submit();
                    break;
                }
            }
        }
        log.warn("could not complete verbindlich Buchen \n{}", Jsoup.parse(driver.getPageSource()).toString());
        throw new IllegalStateException("Could not submit form after " + maxSubmitTries + " tries");
    }

    private void validatePersonenAngaben(WebElement bsForm) {
        StringBuilder message = new StringBuilder();
        List<WebElement> personenAngabenWarnings = bsForm.findElements(By.className("warn"));
        for (WebElement personenAngabenWarning : personenAngabenWarnings) {
            List<WebElement> feldName = bsForm.findElements(By.className("bs_form_sp1"));
            if (feldName.size() > 0) {
                message.append("Invalid PersonenAngabe by '").append(feldName.get(0).getText()).append("'; ");
            } else {
                message.append("Unknown Warning: ").append(personenAngabenWarning.getText()).append("; ");
            }
        }
        if (personenAngabenWarnings.size() > 0) {
            throw new IllegalArgumentException(message.toString());
        }
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

    //    private byte[] readBuchungsBestaetigungAsPdf(String pageSource) {
    //        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    //            com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
    //            PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, out);
    //            pdfDocument.open();
    //            Document bestaetigungDocument = Jsoup.parse(pageSource);
    //            bestaetigungDocument.outputSettings().syntax(Syntax.xml);
    //            byte[] htmlBypes = bestaetigungDocument.html().getBytes();
    //            XMLWorkerHelper xmlWorkerHelper = XMLWorkerHelper.getInstance();
    //            xmlWorkerHelper.parseXHtml(pdfWriter, pdfDocument, new ByteArrayInputStream(htmlBypes));
    //            pdfDocument.close();
    //            pdfWriter.close();
    //            return out.toByteArray();
    //        } catch (Exception e) {
    //            log.error("could not readBestaetigungAsPdf caused by exception ", e);
    //        }
    //        return null;
    //    }

}
