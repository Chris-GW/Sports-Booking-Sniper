package de.chrisgw.sportbooking.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportArt;
import de.chrisgw.sportbooking.model.SportTermin;

import java.util.Set;


public class LazyLoaderFilter extends SimpleBeanPropertyFilter {

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
            throws Exception {
        if (!include(writer)) {
            writer.serializeAsOmittedField(pojo, jgen, provider);
            return;
        }
        String fieldName = writer.getName();
        if (fieldName.equals("sportAngebote")) {
            serializeSportAngeboteAsField(pojo, jgen, provider, writer);
        } else if (fieldName.equals("sportTermine")) {
            serializeSportTermineAsField(pojo, jgen, provider, writer);
        } else {
            writer.serializeAsField(pojo, jgen, provider);
        }
    }

    private void serializeSportAngeboteAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider,
            PropertyWriter writer) throws Exception {
        if (pojo instanceof SportArt) {
            SportArt sportArt = (SportArt) pojo;
            Set<SportAngebot> sportAngebote = sportArt.getSportAngebote();
            if (sportAngebote instanceof SportArtLazyAngebotLoader
                    && !((SportArtLazyAngebotLoader) sportAngebote).isLoaded()) {
                writer.serializeAsOmittedField(pojo, jgen, provider);
                return;
            }
        }
        writer.serializeAsField(pojo, jgen, provider);
    }

    private void serializeSportTermineAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider,
            PropertyWriter writer) throws Exception {
        if (pojo instanceof SportAngebot) {
            SportAngebot sportAngebot = (SportAngebot) pojo;
            Set<SportTermin> sportTermine = sportAngebot.getSportTermine();
            if (sportTermine instanceof SportAngebotLazyTerminLoader
                    && !((SportAngebotLazyTerminLoader) sportTermine).isLoaded()) {
                writer.serializeAsOmittedField(pojo, jgen, provider);
                return;
            }
        }
        writer.serializeAsField(pojo, jgen, provider);
    }


    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return true;
    }

}
