package de.chrisgw.sportbooking.service;

import de.chrisgw.sportbooking.model.SportArt;
import de.chrisgw.sportbooking.model.SportAngebot;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


@Slf4j
public class SportArtLazyAngebotLoader implements Set<SportAngebot> {

    private final SportBookingService sportBookingService;
    private final SportArt sportArt;

    private Set<SportAngebot> sportAngebote;
    private boolean isLoaded;


    public SportArtLazyAngebotLoader(SportBookingService sportBookingService, SportArt sportArt) {
        this.sportBookingService = Objects.requireNonNull(sportBookingService);
        this.sportArt = Objects.requireNonNull(sportArt);
        this.sportAngebote = createLazySportAngebotLoader(sportBookingService, sportArt);
        this.isLoaded = false;
    }


    @SuppressWarnings("unchecked")
    private Set<SportAngebot> createLazySportAngebotLoader(final SportBookingService sportBookingService,
            final SportArt sportArt) {
        return (Set<SportAngebot>) Enhancer.create(Set.class, new LazyLoader() {

            @Override
            public Object loadObject() {
                log.debug("lazy load SportAngebote for {}", sportArt);
                isLoaded = true;
                return sportBookingService.fetchSportAngebote(sportArt);
            }
        });
    }

    public boolean isLoaded() {
        return isLoaded;
    }


    @Override
    public int size() {
        return sportAngebote.size();
    }

    @Override
    public boolean isEmpty() {
        return sportAngebote.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sportAngebote.contains(o);
    }

    @Override
    public Iterator<SportAngebot> iterator() {
        return sportAngebote.iterator();
    }


    @Override
    public Object[] toArray() {
        return sportAngebote.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return sportAngebote.toArray(a);
    }


    @Override
    public boolean add(SportAngebot sportAngebot) {
        return sportAngebote.add(sportAngebot);
    }

    @Override
    public boolean remove(Object o) {
        return sportAngebote.remove(o);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        return sportAngebote.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends SportAngebot> c) {
        return sportAngebote.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sportAngebote.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sportAngebote.removeAll(c);
    }

    @Override
    public void clear() {
        sportAngebote.clear();
    }


    @Override
    public boolean equals(Object o) {
        return sportAngebote.equals(o);
    }

    @Override
    public int hashCode() {
        return sportAngebote.hashCode();
    }

}