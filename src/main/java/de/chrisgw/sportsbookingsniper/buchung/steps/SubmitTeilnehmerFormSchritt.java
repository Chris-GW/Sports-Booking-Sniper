package de.chrisgw.sportsbookingsniper.buchung.steps;

import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsVersuch;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerGender;
import de.chrisgw.sportsbookingsniper.buchung.TeilnehmerKategorie;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.chrisgw.sportsbookingsniper.angebot.HszRwthAachenSportKatalogRepository.DATE_FORMATTER;


@Log4j2
public class SubmitTeilnehmerFormSchritt extends SeleniumSportBuchungsSchritt {

    public SubmitTeilnehmerFormSchritt(WebDriver driver) {
        super(driver);
    }


    @Override
    public boolean isNextBuchungsSchritt(SportBuchungsJob buchungsJob) {
        Optional<WebElement> buchungsForm = findElement(driver, By.name("bsform"));
        Optional<WebElement> vornameInput = buchungsForm.flatMap(findElement(By.name("vorname")));
        return vornameInput.isPresent();
    }

    @Override
    public Stream<SportBuchungsSchritt> possibleNextBuchungsSchritte(SportBuchungsJob buchungsJob) {
        return Stream.of(new ConfirmTeilnehmerFormSchritt(driver));
    }


    @Override
    public SportBuchungsVersuch executeBuchungsSchritt(SportBuchungsJob buchungsJob) {
        new TeilnehmerFormFiller(buchungsJob).fillTeilnehmerForm();
        return super.executeBuchungsSchritt(buchungsJob);
    }


    private class TeilnehmerFormFiller {

        private final SportBuchungsJob buchungsJob;
        private final WebElement bsForm;
        private int teilnehmerIndex;


        public TeilnehmerFormFiller(SportBuchungsJob buchungsJob) {
            this.buchungsJob = buchungsJob;
            this.bsForm = driver.findElement(By.name("bsform"));
        }


        private void fillTeilnehmerForm() {
            List<Teilnehmer> teilnehmerListe = buchungsJob.getTeilnehmerListe();
            for (teilnehmerIndex = 0; teilnehmerIndex < teilnehmerListe.size(); teilnehmerIndex++) {
                Teilnehmer teilnehmer = currentTeilnehmer();
                if (cantEnterMoreTeilnehmer()) {
                    log.warn("fill form with given Teilnehmer[{}] not possible {}", teilnehmerIndex, teilnehmer);
                    continue;
                }
                log.debug("fill form with given Teilnehmer[{}] {}", teilnehmerIndex, teilnehmer);

                findGenderRadioOption().click();
                fillInput("vorname", teilnehmer.getVorname());
                fillInput("name", teilnehmer.getNachname());
                fillInput("strasse", teilnehmer.getStreet());
                fillInput("ort", teilnehmer.getOrt());
                selectTeilnehmerKategorie();
                fillInput("email", teilnehmer.getEmail());
                fillOptionalGeburtsdatum(teilnehmer);
                fillInput("telefon", teilnehmer.getTelefon());
                fillOptionalPaymentAngaben();
            }
            bsForm.findElement(By.name("tnbed")).click();
            submitTeilnehmerForm();
        }

        private void fillOptionalGeburtsdatum(Teilnehmer teilnehmer) {
            List<WebElement> geburtsdatumInputs = bsForm.findElements(byNameX("geburtsdatum"));
            if (geburtsdatumInputs.isEmpty()) {
                return;
            }
            LocalDate geburtsDatum = teilnehmer.getGeburtsDatum();
            if (geburtsDatum != null) {
                fillInput("geburtsdatum", DATE_FORMATTER.format(geburtsDatum));
            } else {
                fillInput("geburtsdatum", null);
            }
        }

        private boolean cantEnterMoreTeilnehmer() {
            return bsForm.findElements(byNameX(("sex"))).isEmpty();
        }


        private WebElement findGenderRadioOption() {
            TeilnehmerGender gender = currentTeilnehmer().getGender();
            List<WebElement> genderInputs = bsForm.findElements(byNameX(("sex")));
            return genderInputs.stream()
                    .filter(isGenderRadioOption(gender))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("could not select gender radio option for " + gender));
        }

        private Predicate<WebElement> isGenderRadioOption(TeilnehmerGender gender) {
            return genderRadioOption -> {
                String genderShortName = gender.getShortName();
                String optionValue = genderRadioOption.getAttribute("value");
                return genderShortName.equalsIgnoreCase(optionValue);
            };
        }


        private void selectTeilnehmerKategorie() {
            Teilnehmer teilnehmer = currentTeilnehmer();
            TeilnehmerKategorie teilnehmerKategorie = teilnehmer.getTeilnehmerKategorie();

            Select teilnehmerKategorieSelect = new Select(bsForm.findElement(byNameX("statusorig")));
            log.trace("selectTeilnehmerKategorie {} byValue={}", teilnehmerKategorie, teilnehmerKategorie.getValue());
            teilnehmerKategorieSelect.selectByValue(teilnehmerKategorie.getValue());

            switch (teilnehmerKategorie) {
                case STUDENT_FH:
                case STUDENT_RWTH:
                case STUDENT_NRW:
                case STUDENT_ANDERE_HOCHSCHULE:
                    String matrikelnummer = teilnehmer.getMatrikelnummer();
                    log.trace("enter additional matrikelnummer={} for {}", matrikelnummer, teilnehmerKategorie);
                    fillInput("matnr", matrikelnummer);
                    break;

                case MITARBEITER_FH:
                case MITARBEITER_RWTH:
                case MITARBEITER_KLINIKUM:
                    String mitarbeiterNummer = teilnehmer.getMitarbeiterNummer();
                    log.trace("enter additional mitarbeiterNummer={} for {}", mitarbeiterNummer, teilnehmerKategorie);
                    fillInput("mitnr", mitarbeiterNummer);
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


        private void fillOptionalPaymentAngaben() {
            Teilnehmer teilnehmer = currentTeilnehmer();
            if (buchungsJob.getSportAngebot().isPaymentRequierd()) {
                fillInput("iban", teilnehmer.getIban());
                if (teilnehmer.getKontoInhaber() != null) {
                    fillInput("kontoinh", teilnehmer.getKontoInhaber());
                }
            }
        }


        private void submitTeilnehmerForm() {
            WebElement bsForm = driver.findElement(By.name("bsform"));
            WebElement bsFormFooter = bsForm.findElement(By.id("bs_foot"));
            List<WebElement> bsFormButtons = bsFormFooter.findElements(By.tagName("input"));
            WebElement weiterZurBuchungBtn = bsFormButtons.stream()
                    .filter(this::isWeiterZurBuchungBsFormBtn)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No 'weiter zur Buchung' submit Button found"));
            for (int i = 0; !trySubmitTillStaleness(weiterZurBuchungBtn); i++) {
                log.trace(i + " submit");
            }
        }

        private boolean trySubmitTillStaleness(WebElement weiterZurBuchungBtn) {
            try {
                weiterZurBuchungBtn.submit();
                validateTeilnehmerForm();
                return new WebDriverWait(driver, Duration.ofMillis(1000))
                        .until(ExpectedConditions.stalenessOf(weiterZurBuchungBtn));
            } catch (TimeoutException e) {
                return false;
            }
        }

        private boolean isWeiterZurBuchungBsFormBtn(WebElement bsFormButton) {
            String value = bsFormButton.getAttribute("value");
            return "weiter zur Buchung".equalsIgnoreCase(value);
        }

        private void validateTeilnehmerForm() {
            StringBuilder message = new StringBuilder();
            WebElement bsForm = driver.findElement(By.name("bsform"));
            List<WebElement> teilnehmerAngabenWarnings = bsForm.findElements(By.className("warn"));
            if (teilnehmerAngabenWarnings.isEmpty()) {
                return;
            }

            for (WebElement teilnehmerAngabenWarning : teilnehmerAngabenWarnings) {
                List<WebElement> feldName = teilnehmerAngabenWarning.findElements(By.className("bs_form_sp1"));
                if (feldName.isEmpty()) {
                    message.append("Unknown Warning: ").append(teilnehmerAngabenWarning.getText()).append("; ");
                } else {
                    message.append("Invalid Teilnehmer by '").append(feldName.get(0).getText()).append("'; ");
                }
            }
            throw new IllegalArgumentException(message.toString());
        }


        private Teilnehmer currentTeilnehmer() {
            List<Teilnehmer> teilnehmerListe = buchungsJob.getTeilnehmerListe();
            return teilnehmerListe.get(teilnehmerIndex);
        }


        private void fillInput(String inputName, String value) {
            WebElement inputElement = bsForm.findElement(byNameX(inputName));
            inputElement.clear();
            if (value != null) {
                inputElement.sendKeys(value);
            } else {
                log.warn("tried to fillInput {} with sendKeys({})", inputName, value);
            }
        }

        private By byNameX(String inputName) {
            if (teilnehmerIndex > 0) {
                inputName += "_" + teilnehmerIndex;
            }
            return By.name(inputName);
        }

    }

}
