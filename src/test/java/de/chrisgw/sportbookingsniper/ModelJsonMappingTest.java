package de.chrisgw.sportbookingsniper;

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
import de.chrisgw.sportbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportbookingsniper.angebot.SportArt;
import de.chrisgw.sportbookingsniper.angebot.SportKatalog;
import de.chrisgw.sportbookingsniper.buchung.TeilnehmerAngaben;
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
import static de.chrisgw.sportbookingsniper.SportBookingModelTestUtil.createFreitagsSportAngebot;
import static de.chrisgw.sportbookingsniper.SportBookingModelTestUtil.createMontagsSportAngebot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class ModelJsonMappingTest {

    static final Logger logger = LoggerFactory.getLogger(ModelJsonMappingTest.class);

    private ObjectMapper objectMapper;

    private TeilnehmerAngaben teilnehmerAngaben;
    private SportKatalog sportKatalog;

    private SportArt badminton;
    private SportArt volleyball;


    @Before
    public void setUp() {
        setUpJackson();

        teilnehmerAngaben = SportBookingModelTestUtil.createPersonenAngaben();
        sportKatalog = new SportKatalog();

        badminton = new SportArt("Badminton", "http://www.badminton.de");
        badminton.addSportAngebot(createMontagsSportAngebot(badminton));
        badminton.addSportAngebot(createFreitagsSportAngebot(badminton));
        sportKatalog.addSportArt(badminton);

        volleyball = new SportArt("Volleyball", "http://www.volleyball.de");
        volleyball.addSportAngebot(createFreitagsSportAngebot(volleyball));
        sportKatalog.addSportArt(volleyball);
    }


    @Test
    public void shouldSeralizePersonenAngaben() throws Exception {
        TeilnehmerAngaben teilnehmerAngaben = SportBookingModelTestUtil.createPersonenAngaben();

        String json = objectMapper.writeValueAsString(teilnehmerAngaben);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.vorname", is(teilnehmerAngaben.getVorname())));
        assertThat(json, hasJsonPath("$.nachname", is(teilnehmerAngaben.getNachname())));
        assertThat(json, hasJsonPath("$.email", is(teilnehmerAngaben.getEmail())));
        assertThat(json, hasJsonPath("$.gender", jsonValue(teilnehmerAngaben.getGender())));

        assertThat(json, hasJsonPath("$.street", is(teilnehmerAngaben.getStreet())));
        assertThat(json, hasJsonPath("$.ort", is(teilnehmerAngaben.getOrt())));

        assertThat(json, hasJsonPath("$.personKategorie", jsonValue(teilnehmerAngaben.getTeilnehmerKategorie())));
        assertThat(json, hasJsonPath("$.matrikelnummer", is(teilnehmerAngaben.getMatrikelnummer())));
        assertThat(json, hasJsonPath("$.mitarbeiterNummer", is(teilnehmerAngaben.getMitarbeiterNummer())));
    }

    @Test
    public void shouldReadPersonenAngaben() throws Exception {
        TeilnehmerAngaben teilnehmerAngaben = this.teilnehmerAngaben;
        String json = objectMapper.writeValueAsString(teilnehmerAngaben);
        logger.debug(json);

        TeilnehmerAngaben readedTeilnehmerAngaben = objectMapper.readValue(json, TeilnehmerAngaben.class);
        logger.debug("{}", readedTeilnehmerAngaben);
        assertThat(readedTeilnehmerAngaben, equalTo(teilnehmerAngaben));
    }


    @Test
    public void shouldSeralizeSportKatalog() throws Exception {
        SportKatalog sportKatalog = this.sportKatalog;
        String json = objectMapper.writeValueAsString(sportKatalog);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.abrufzeitpunkt", jsonValue(sportKatalog.getAbrufzeitpunkt())));
        for (SportArt sportArt : sportKatalog.getSportArten()) {
            assertThat(json, hasJsonPath("$.sportArten[*].name", hasItem(sportArt.getName())));
            assertThat(json, hasJsonPath("$.sportArten[*].url", hasItem(sportArt.getUrl())));
        }
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
