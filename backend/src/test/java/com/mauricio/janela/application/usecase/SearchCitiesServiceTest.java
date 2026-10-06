package com.mauricio.janela.application.usecase;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SearchCitiesServiceTest {

    @Test
    void keepsOnlyTheFirstOfPlacesWithSameNameStateAndCountry() {
        List<Location> results = List.of(
                place(1, "São José", "Santa Catarina", "BR"),
                place(2, "São José", "Santa Catarina", "BR"),
                place(3, "São José", "Saint Joseph", "DM"),
                place(4, "São José de Princesa", "Paraíba", "BR"));

        List<Location> suggestions = new SearchCitiesService(geocoderReturning(results))
                .searchCities(" São José ", Language.PT);

        assertThat(suggestions).extracting(Location::id).containsExactly(1L, 3L, 4L);
    }

    @Test
    void returnsAtMostSixSuggestions() {
        List<Location> results = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> place(i, "Santa Rita " + i, "Paraíba", "BR"))
                .toList();

        assertThat(new SearchCitiesService(geocoderReturning(results)).searchCities("Santa Rita", Language.PT))
                .hasSize(SearchCitiesService.MAX_SUGGESTIONS);
    }

    private static Location place(long id, String name, String admin1, String countryCode) {
        return new Location(id, name, admin1, null, countryCode, 0, 0, "UTC");
    }

    private static GeocodingProvider geocoderReturning(List<Location> results) {
        return new GeocodingProvider() {
            @Override
            public Optional<Location> findByName(String city) {
                return Optional.empty();
            }

            @Override
            public Optional<Location> findById(long id) {
                return Optional.empty();
            }

            @Override
            public List<Location> search(String query, int limit, Language language) {
                assertThat(query).isEqualTo(query.strip());
                return results.stream().limit(limit).toList();
            }
        };
    }
}
