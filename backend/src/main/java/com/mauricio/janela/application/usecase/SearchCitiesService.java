package com.mauricio.janela.application.usecase;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.in.SearchCitiesUseCase;
import com.mauricio.janela.domain.port.out.GeocodingProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Suggests cities while the person types, so a name shared by several places can be told apart. The geocoder
 * sometimes lists one city twice (municipality and locality); only the best-ranked of those is kept.
 */
public class SearchCitiesService implements SearchCitiesUseCase {

    public static final int MAX_SUGGESTIONS = 6;
    /** Asks for a few extra places so duplicates don't leave the list short. */
    private static final int LOOKUP_LIMIT = 10;

    private final GeocodingProvider geocodingProvider;

    public SearchCitiesService(GeocodingProvider geocodingProvider) {
        this.geocodingProvider = geocodingProvider;
    }

    @Override
    public List<Location> searchCities(String query, Language language) {
        Set<List<String>> seen = new HashSet<>();
        return geocodingProvider.search(query.strip(), LOOKUP_LIMIT, language).stream()
                .filter(place -> seen.add(sameCityKey(place)))
                .limit(MAX_SUGGESTIONS)
                .toList();
    }

    private static List<String> sameCityKey(Location place) {
        return List.of(Objects.toString(place.name(), ""), Objects.toString(place.admin1(), ""),
                Objects.toString(place.countryCode(), ""));
    }
}
