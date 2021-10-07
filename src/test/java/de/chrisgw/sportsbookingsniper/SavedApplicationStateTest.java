package de.chrisgw.sportsbookingsniper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import de.chrisgw.sportsbookingsniper.buchung.strategie.KonfigurierbareSportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.buchung.strategie.SportBuchungsStrategie;
import de.chrisgw.sportsbookingsniper.gui.state.SavedApplicationState;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil.newSportKatalog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class SavedApplicationStateTest {

    static final Logger logger = LoggerFactory.getLogger(SavedApplicationState.class);

    private final ObjectMapper objectMapper = SportBookingSniperApplication.createObjectMapper();

    private SportKatalog sportKatalog;
    private Teilnehmer teilnehmer;

    private SportArt badminton;
    private SportArt volleyball;


    @Before
    public void setUp() {
        setUpJackson();

        teilnehmer = SportBookingModelTestUtil.newTeilnehmer();
        sportKatalog = newSportKatalog();
        badminton = sportKatalog.findSportArtByName("Badminton Karte Text").orElseThrow(RuntimeException::new);
        volleyball = sportKatalog.findSportArtByName("Volleyball Level 1 Text").orElseThrow(RuntimeException::new);
    }


    @Test
    public void shouldSeralizeTeilnehmer() throws Exception {
        Teilnehmer teilnehmer = SportBookingModelTestUtil.newTeilnehmer();

        String json = objectMapper.writeValueAsString(teilnehmer);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.vorname", is(teilnehmer.getVorname())));
        assertThat(json, hasJsonPath("$.nachname", is(teilnehmer.getNachname())));
        assertThat(json, hasJsonPath("$.street", is(teilnehmer.getStreet())));
        assertThat(json, hasJsonPath("$.ort", is(teilnehmer.getOrt())));
        assertThat(json, hasJsonPath("$.email", is(teilnehmer.getEmail())));
        assertThat(json, hasJsonPath("$.telefon", is(teilnehmer.getTelefon())));
        assertThat(json, hasJsonPath("$.geburtsDatum", jsonValue(teilnehmer.getGeburtsDatum())));
        assertThat(json, hasJsonPath("$.gender", jsonValue(teilnehmer.getGender())));

        assertThat(json, hasJsonPath("$.teilnehmerKategorie", jsonValue(teilnehmer.getTeilnehmerKategorie())));
        assertThat(json, hasJsonPath("$.matrikelnummer", is(teilnehmer.getMatrikelnummer())));
        assertThat(json, hasJsonPath("$.mitarbeiterNummer", is(teilnehmer.getMitarbeiterNummer())));

        assertThat(json, hasJsonPath("$.iban", is(teilnehmer.getIban())));
        assertThat(json, hasJsonPath("$.kontoInhaber", is(teilnehmer.getKontoInhaber())));
    }

    @Test
    public void shouldReadTeilnehmer() throws Exception {
        Teilnehmer teilnehmer = this.teilnehmer;
        String json = objectMapper.writeValueAsString(teilnehmer);
        logger.debug(json);

        Teilnehmer readedTeilnehmer = objectMapper.readValue(json, Teilnehmer.class);
        logger.debug("{}", readedTeilnehmer);
        assertThat(readedTeilnehmer, equalTo(teilnehmer));
    }


    @Test
    public void shouldSeralizeSavedApplicationState() throws Exception {
        var applicationState = new SavedApplicationState();
        applicationState.setNextJobId(52);
        applicationState.getTeilnehmerListe().add(teilnehmer);
        applicationState.getPendingBuchungsJobs().add(SportBookingModelTestUtil.newSportBuchungsJob());
        applicationState.getPendingBuchungsJobs().add(SportBookingModelTestUtil.newSportBuchungsJob());

        String json = objectMapper.writeValueAsString(applicationState);
        logger.debug(json);
        System.out.println(json);

        var readApplicationState = objectMapper.readValue(json, SavedApplicationState.class);
        logger.debug("{}", readApplicationState);
        assertThat(readApplicationState, hasProperty("versionUID", is(applicationState.getVersionUID())));
        assertThat(readApplicationState, hasProperty("nextJobId", is(applicationState.getNextJobId())));
        assertThat(readApplicationState, hasProperty("firstVisite", is(applicationState.isFirstVisite())));
        assertThat(readApplicationState, hasProperty("saveTime", is(applicationState.getSaveTime())));
        assertThat(readApplicationState, hasProperty("language", is(applicationState.getLanguage())));
        assertThat(readApplicationState, hasProperty("selectedTheme", is(applicationState.getSelectedTheme())));

        List<Teilnehmer> teilnehmerListe = readApplicationState.getTeilnehmerListe();
        assertThat(teilnehmerListe, hasItem(teilnehmer));
        assertThat(teilnehmerListe, hasSize(applicationState.getTeilnehmerListe().size()));
        assertThat(readApplicationState, equalTo(applicationState));
    }


    @Test
    public void shouldSeralizeSportBuchungsStrategie() throws Exception {
        SportBuchungsStrategie buchungsStrategie = KonfigurierbareSportBuchungsStrategie.defaultKonfiguration();
        String json = objectMapper.writeValueAsString(buchungsStrategie);
        logger.debug(json);
        SportBuchungsStrategie readedBuchungsStrategie = objectMapper.readValue(json, SportBuchungsStrategie.class);
        logger.debug("{}", readedBuchungsStrategie);
        assertThat(readedBuchungsStrategie, equalTo(buchungsStrategie));
    }


    private void setUpJackson() {
        Configuration.setDefaults(new Defaults() {

            @Override
            public JsonProvider jsonProvider() {
                return new JacksonJsonProvider(objectMapper);
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new JacksonMappingProvider(objectMapper);
            }
        });
    }


    public <T> Matcher<T> jsonValue(T value) {
        return new BaseMatcher<T>() {

            @Override
            public boolean matches(Object item) {
                if (value == null) {
                    return item == null;
                }
                return value.equals(convertValue(item));
            }

            private Object convertValue(Object item) {
                return objectMapper.convertValue(item, value.getClass());
            }


            @Override
            public void describeTo(Description description) {
                description.appendValue(value);
            }

        };
    }

}
