package de.chrisgw;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import de.chrisgw.sportbooking.model.*;
import de.chrisgw.sportbooking.service.LazyLoaderFilter;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class ModelJsonMappingTest {

    static final Logger logger = LoggerFactory.getLogger(ModelJsonMappingTest.class);

    private ObjectMapper objectMapper;

    private PersonenAngaben personenAngaben;
    private SportKatalog sportKatalog;

    private SportArt badminton;
    private SportArt volleyball;


    @Before
    public void setUp() {
        setUpJackson();

        personenAngaben = SportBookingModelTestUtil.createPersonenAngaben();
        sportKatalog = new SportKatalog();

        badminton = new SportArt("Badminton", "http://www.badminton.de");
        badminton.addSportAngebot(SportBookingModelTestUtil.createMontagsSportAngebot(badminton));
        badminton.addSportAngebot(SportBookingModelTestUtil.createFreitagsSportAngebot(badminton));
        sportKatalog.addSportArt(badminton);

        volleyball = new SportArt("Volleyball", "http://www.volleyball.de");
        volleyball.addSportAngebot(SportBookingModelTestUtil.createFreitagsSportAngebot(volleyball));
        sportKatalog.addSportArt(volleyball);
    }


    @Test
    public void shouldSeralizePersonenAngaben() throws Exception {
        PersonenAngaben personenAngaben = SportBookingModelTestUtil.createPersonenAngaben();

        String json = objectMapper.writeValueAsString(personenAngaben);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.vorname", is(personenAngaben.getVorname())));
        assertThat(json, hasJsonPath("$.nachname", is(personenAngaben.getNachname())));
        assertThat(json, hasJsonPath("$.email", is(personenAngaben.getEmail())));
        assertThat(json, hasJsonPath("$.gender", jsonValue(personenAngaben.getGender())));

        assertThat(json, hasJsonPath("$.street", is(personenAngaben.getStreet())));
        assertThat(json, hasJsonPath("$.ort", is(personenAngaben.getOrt())));

        assertThat(json, hasJsonPath("$.personKategorie", jsonValue(personenAngaben.getPersonKategorie())));
        assertThat(json, hasJsonPath("$.matrikelnummer", is(personenAngaben.getMatrikelnummer())));
        assertThat(json, hasJsonPath("$.mitarbeiterNummer", is(personenAngaben.getMitarbeiterNummer())));
    }

    @Test
    public void shouldReadPersonenAngaben() throws Exception {
        PersonenAngaben personenAngaben = this.personenAngaben;
        String json = objectMapper.writeValueAsString(personenAngaben);
        logger.debug(json);

        PersonenAngaben readedPersonenAngaben = objectMapper.readValue(json, PersonenAngaben.class);
        logger.debug("{}", readedPersonenAngaben);
        assertThat(readedPersonenAngaben, equalTo(personenAngaben));
    }


    @Test
    public void shouldSeralizeSportKatalog() throws Exception {
        SportKatalog sportKatalog = this.sportKatalog;
        String json = objectMapper.writeValueAsString(sportKatalog);
        logger.debug(json);

        assertThat(json, hasJsonPath("$.uhrzeitAberufen", jsonValue(sportKatalog.getUhrzeitAberufen())));
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
        assertThat(readedSportKatalog, hasProperty("uhrzeitAberufen", is(sportKatalog.getUhrzeitAberufen())));
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
    public void shouldSeralizeLazySportArt() throws Exception {
        SportArt sportArt = SportBookingModelTestUtil.createLazySportArt();
        String json = objectMapper.writeValueAsString(sportArt);
        logger.debug(json);

        assertThat(json, isJson(withoutJsonPath("$.sportAngebote")));
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
    public void shouldReadLazySportArt() throws Exception {
        SportArt sportArt = SportBookingModelTestUtil.createLazySportArt();
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

        LazyLoaderFilter lazyLoaderFilter = new LazyLoaderFilter();
        FilterProvider filters = new SimpleFilterProvider().addFilter("lazyLoaderFilter", lazyLoaderFilter);
        objectMapper.setFilterProvider(filters);

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
