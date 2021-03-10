package de.chrisgw.sportsbookingsniper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportArt;
import de.chrisgw.sportsbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsWiederholungStrategie;
import de.chrisgw.sportsbookingsniper.buchung.KonfigurierbareSportBuchungsWiederholungStrategie;
import de.chrisgw.sportsbookingsniper.buchung.Teilnehmer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static de.chrisgw.sportsbookingsniper.SportBookingModelTestUtil.newSportKatalog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class ModelJsonMappingTest {

    static final Logger logger = LoggerFactory.getLogger(ModelJsonMappingTest.class);

    private ObjectMapper objectMapper;

    private Teilnehmer teilnehmer;
    private SportKatalog sportKatalog;

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
        assertThat(json, hasJsonPath("$.email", is(teilnehmer.getEmail())));
        assertThat(json, hasJsonPath("$.gender", jsonValue(teilnehmer.getGender())));

        assertThat(json, hasJsonPath("$.street", is(teilnehmer.getStreet())));
        assertThat(json, hasJsonPath("$.ort", is(teilnehmer.getOrt())));

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
    public void shouldReadSportKatalog() throws Exception {
        SportKatalog sportKatalog = this.sportKatalog;
        String json = objectMapper.writeValueAsString(sportKatalog);
        logger.debug(json);

        SportKatalog readedSportKatalog = objectMapper.readValue(json, SportKatalog.class);
        logger.debug("{}", readedSportKatalog);
        assertThat(readedSportKatalog, hasProperty("abrufzeitpunkt", is(sportKatalog.getAbrufzeitpunkt())));
        for (SportArt sportArt : sportKatalog.getSportArten()) {
            assertThat(readedSportKatalog.getSportArten(), hasItem(sportArt));
        }
    }


    @Test
    public void shouldSeralizeSportArt() throws Exception {
        SportArt sportArt = this.badminton;
        String json = objectMapper.writeValueAsString(sportArt);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.name", jsonValue(sportArt.getName())));
        assertThat(json, hasJsonPath("$.url", jsonValue(sportArt.getUrl())));
    }


    @Test
    public void shouldReadSportArt() throws Exception {
        SportArt sportArt = this.badminton;
        String json = objectMapper.writeValueAsString(sportArt);
        logger.debug(json);

        SportArt readedSportArt = objectMapper.readValue(json, SportArt.class);
        logger.debug("{}", readedSportArt);
        assertThat(readedSportArt, equalTo(sportArt));
    }


    @Test
    public void shouldSeralizeSportAngebot() throws Exception {
        for (SportAngebot sportAngebot : badminton.getSportAngebote()) {
            String json = objectMapper.writeValueAsString(sportAngebot);
            logger.debug(json);

            assertThat(json, hasJsonPath("$.kursnummer", jsonValue(sportAngebot.getKursnummer())));
            // TODO
        }
    }

    @Test
    public void shouldReadSportAngebot() throws Exception {
        for (SportAngebot sportAngebot : badminton.getSportAngebote()) {
            String json = objectMapper.writeValueAsString(sportAngebot);
            logger.debug(json);

            SportAngebot readedSportAngebot = objectMapper.readValue(json, SportAngebot.class);
            logger.debug("{}", readedSportAngebot);
            assertThat(readedSportAngebot, equalTo(sportAngebot));
        }
    }


    @Test
    public void shouldSeralizeSportBuchungsStrategie() throws Exception {
        SportBuchungsWiederholungStrategie buchungsStrategie = KonfigurierbareSportBuchungsWiederholungStrategie.defaultKonfiguration();
        String json = objectMapper.writeValueAsString(buchungsStrategie);
        logger.debug(json);
        SportBuchungsWiederholungStrategie readedBuchungsStrategie = objectMapper.readValue(json,
                SportBuchungsWiederholungStrategie.class);
        logger.debug("{}", readedBuchungsStrategie);
        assertThat(readedBuchungsStrategie, equalTo(buchungsStrategie));
    }


    private void setUpJackson() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
