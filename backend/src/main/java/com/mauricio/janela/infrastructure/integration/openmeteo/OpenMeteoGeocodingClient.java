package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.infrastructure.config.OpenMeteoConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class OpenMeteoGeocodingClient implements GeocodingProvider {

    private final RestClient restClient;

    public OpenMeteoGeocodingClient(@Qualifier(OpenMeteoConfig.GEOCODING_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<Location> findByName(String city) {
        return search(city, 1, Language.PT).stream().findFirst();
    }

    @Override
    public Optional<Location> findById(long id) {
        GeocodingResponse.Result result;
        try {
            result = restClient.get()
                    .uri(uri -> uri.path("/get").queryParam("id", id).queryParam("language", "pt").build())
                    .retrieve()
                    .body(GeocodingResponse.Result.class);
        } catch (HttpClientErrorException e) {
            // Open-Meteo answers 400 for an unknown id.
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new WeatherUnavailableException("Open-Meteo geocoding request failed", e);
        } catch (RestClientException e) {
            throw new WeatherUnavailableException("Open-Meteo geocoding request failed", e);
        }
        return Optional.ofNullable(result)
                .filter(GeocodingResponse.Result::hasCoordinates)
                .map(GeocodingResponse.Result::toLocation);
    }

    @Override
    public List<Location> search(String query, int limit, Language language) {
        GeocodingResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("name", query)
                            .queryParam("count", limit)
                            .queryParam("language", language.code())
                            .build())
                    .retrieve()
                    .body(GeocodingResponse.class);
        } catch (RestClientException e) {
            throw new WeatherUnavailableException("Open-Meteo geocoding request failed", e);
        }

        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream()
                .filter(GeocodingResponse.Result::hasCoordinates)
                .map(GeocodingResponse.Result::toLocation)
                .toList();
    }
}
