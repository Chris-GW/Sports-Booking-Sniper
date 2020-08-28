package de.chrisgw.sportbookingsniper.buchung.steps;

import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerKategorie;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsVersuch;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Slf4j
public class SubmitTeilnehmerAngabenSchritt extends SeleniumSportBuchungsSchritt {

    public SubmitTeilnehmerAngabenSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> buchungsForm = findElement(driver, By.name("bsform"));
        Optional<WebElement> vornameInput = buchungsForm.flatMap(findElement(By.name("vorname")));
        return vornameInput.isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte() {
        return Stream.of(new ConfirmTeilnehmerAngabenSchritt(driver));
    }

    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        TeilnehmerAngaben teilnehmerAngaben = buchungsJob.getTeilnehmerAngaben();
        log.debug("fill form with given TeilnehmerAngaben {}", teilnehmerAngaben);
        WebElement bsform = driver.findElement(By.name("bsform"));
        selectGenderRadio(bsform, teilnehmerAngaben);

        bsform.findElement(By.name("vorname")).clear();
        bsform.findElement(By.name("vorname")).sendKeys(teilnehmerAngaben.getVorname());
        bsform.findElement(By.name("name")).clear();
        bsform.findElement(By.name("name")).sendKeys(teilnehmerAngaben.getNachname());
        bsform.findElement(By.name("email")).clear();
        bsform.findElement(By.name("email")).sendKeys(teilnehmerAngaben.getEmail());
        if (teilnehmerAngaben.getTelefon() != null) {
            bsform.findElement(By.name("telefon")).sendKeys(teilnehmerAngaben.getTelefon());
        }

        bsform.findElement(By.name("strasse")).clear();
        bsform.findElement(By.name("strasse")).sendKeys(teilnehmerAngaben.getStreet());
        bsform.findElement(By.name("ort")).clear();
        bsform.findElement(By.name("ort")).sendKeys(teilnehmerAngaben.getOrt());

        selectTeilnehmerKategorie(bsform, teilnehmerAngaben);

        if (buchungsJob.getSportAngebot().isPaymentRequierd()) {
            bsform.findElement(By.name("iban")).clear();
            bsform.findElement(By.name("iban")).sendKeys(teilnehmerAngaben.getIban());
            if (teilnehmerAngaben.getKontoInhaber() != null) {
                bsform.findElement(By.name("kontoinh")).clear();
                bsform.findElement(By.name("kontoinh")).sendKeys(teilnehmerAngaben.getKontoInhaber());
            }
        }
        bsform.findElement(By.name("tnbed")).click();

        submitTeilnehmerAngabenForm();
        return super.executeBuchungsSchritt(buchungsJob);
    }

    private Optional<WebElement> selectGenderRadio(WebElement bsform, TeilnehmerAngaben teilnehmerAngaben) {
        String genderShortName = teilnehmerAngaben.getGender().getShortName();
        List<WebElement> genderInputs = bsform.findElements(By.cssSelector("input[name='sex']"));
        for (WebElement genderInput : genderInputs) {
            String value = genderInput.getAttribute("value");
            if (value.equalsIgnoreCase(genderShortName)) {
                genderInput.click();
                return Optional.of(genderInput);
            }
        }
        return Optional.empty();
    }

    private void selectTeilnehmerKategorie(WebElement teilnehmerAngabenForm, TeilnehmerAngaben teilnehmerAngaben) {
        TeilnehmerKategorie teilnehmerKategorie = teilnehmerAngaben.getTeilnehmerKategorie();
        Select teilnehmerKategorieSelect = new Select(teilnehmerAngabenForm.findElement(By.name("statusorig")));
        log.trace("selectTeilnehmerKategorie {} byValue={}", teilnehmerKategorie, teilnehmerKategorie.getValue());
        teilnehmerKategorieSelect.selectByValue(teilnehmerKategorie.getValue());

        switch (teilnehmerKategorie) {
        case STUDENT_FH:
        case STUDENT_RWTH:
        case STUDENT_NRW:
        case STUDENT_ANDERE_HOCHSCHULE:
            String matrikelnummer = teilnehmerAngaben.getMatrikelnummer();
            log.trace("enter additional matrikelnummer={} for {}", matrikelnummer, teilnehmerKategorie);
            teilnehmerAngabenForm.findElement(By.name("matnr")).clear();
            teilnehmerAngabenForm.findElement(By.name("matnr")).sendKeys(matrikelnummer);
            break;

        case MITARBEITER_FH:
        case MITARBEITER_RWTH:
        case MITARBEITER_KLINIKUM:
            String mitarbeiterNummer = teilnehmerAngaben.getMitarbeiterNummer();
            log.trace("enter additional mitarbeiterNummer={} for {}", mitarbeiterNummer, teilnehmerKategorie);
            teilnehmerAngabenForm.findElement(By.name("mitnr")).clear();
            teilnehmerAngabenForm.findElement(By.name("mitnr")).sendKeys(mitarbeiterNummer);
            break;

        case SCHUELER:
        case AZUBI_RWTH_UKA:
        case EXTERN:
            log.trace("no additional data entered for {}", teilnehmerKategorie);
            break;
        default:
            throw new IllegalArgumentException("Unknown TeilnehmerKategorie " + teilnehmerKategorie);
        }
    }


    private void submitTeilnehmerAngabenForm() {
        WebElement bsForm = driver.findElement(By.name("bsform"));
        WebElement bsFormFooter = bsForm.findElement(By.id("bs_foot"));
        List<WebElement> bsFormButtons = bsFormFooter.findElements(By.tagName("input"));
        WebElement weiterZurBuchungBtn = bsFormButtons.stream()
                .filter(this::isWeiterZurBuchungBsFormBtn)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No 'weiter zur Buchung' submit Button found"));
        weiterZurBuchungBtn.submit();
        validatePersonenAngaben(bsForm);
    }

    private boolean isWeiterZurBuchungBsFormBtn(WebElement bsFormButton) {
        String value = bsFormButton.getAttribute("value");
        return "weiter zur Buchung".equalsIgnoreCase(value);
    }

    private void validatePersonenAngaben(WebElement bsForm) {
        StringBuilder message = new StringBuilder();
        List<WebElement> teilnehmerAngabenWarnings = bsForm.findElements(By.className("warn"));
        if (teilnehmerAngabenWarnings.isEmpty()) {
            return;
        }

        for (WebElement teilnehmerAngabenWarning : teilnehmerAngabenWarnings) {
            List<WebElement> feldName = bsForm.findElements(By.className("bs_form_sp1"));
            if (feldName.isEmpty()) {
                message.append("Unknown Warning: ").append(teilnehmerAngabenWarning.getText()).append("; ");
            } else {
                message.append("Invalid TeilnehmerAngaben by '").append(feldName.get(0).getText()).append("'; ");
            }
        }
        throw new IllegalArgumentException(message.toString());
    }

}
