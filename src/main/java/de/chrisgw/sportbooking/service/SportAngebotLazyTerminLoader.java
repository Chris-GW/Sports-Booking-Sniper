package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportAngebot;
import de.chrisgw.sportbooking.model.SportTermin;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SportAngebotLazyTerminLoader implements Set<SportTermin> {

    private static final Logger logger = LoggerFactory.getLogger(SportAngebotLazyTerminLoader.class);

    private final SportBookingService sportBookingService;
    private final SportAngebot sportAngebot;

    private Set<SportTermin> sportTermine;
    private boolean isLoaded;


    public SportAngebotLazyTerminLoader(SportBookingService sportBookingService, SportAngebot sportAngebot) {
        this.sportBookingService = Objects.requireNonNull(sportBookingService);
        this.sportAngebot = Objects.requireNonNull(sportAngebot);
        this.sportTermine = createTerminLazyLoader(sportBookingService, sportAngebot);
        this.isLoaded = false;
    }


    @SuppressWarnings("unchecked")
    private Set<SportTermin> createTerminLazyLoader(final SportBookingService sportBookingService,
            final SportAngebot sportAngebot) {
        return (Set<SportTermin>) Enhancer.create(Set.class, new LazyLoader() {

            @Override
            public Object loadObject() {
                logger.debug("lazy load SportTermine for {}", sportAngebot);
                isLoaded = true;
                return sportBookingService.fetchSportTermine(sportAngebot);
            }
        });
    }

    public boolean isLoaded() {
        return isLoaded;
    }


    @Override
    public int size() {
        return sportTermine.size();
    }

    @Override
    public boolean isEmpty() {
        return sportTermine.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sportTermine.contains(o);
    }

    @Override
    public Iterator<SportTermin> iterator() {
        return sportTermine.iterator();
    }


    @Override
    public Object[] toArray() {
        return sportTermine.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return sportTermine.toArray(a);
    }


    @Override
    public boolean add(SportTermin sportTermin) {
        return sportTermine.add(sportTermin);
    }

    @Override
    public boolean remove(Object o) {
        return sportTermine.remove(o);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        return sportTermine.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends SportTermin> c) {
        return sportTermine.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sportTermine.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sportTermine.removeAll(c);
    }

    @Override
    public void clear() {
        sportTermine.clear();
    }


    @Override
    public boolean equals(Object o) {
        return sportTermine.equals(o);
    }

    @Override
    public int hashCode() {
        return sportTermine.hashCode();
    }

}
