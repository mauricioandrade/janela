package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.infrastructure.config.OpenMeteoConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class OpenMeteoGeocodingClient implements GeocodingProvider {

    private final RestClient restClient;

    public OpenMeteoGeocodingClient(@Qualifier(OpenMeteoConfig.GEOCODING_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<Location> findByName(String city) {
        GeocodingResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("name", city)
                            .queryParam("count", 1)
                            .queryParam("language", "pt")
                            .build())
                    .retrieve()
                    .body(GeocodingResponse.class);
        } catch (RestClientException e) {
            throw new WeatherUnavailableException("Open-Meteo geocoding request failed", e);
        }

        if (response == null || response.results() == null) {
            return Optional.empty();
        }
        return response.results().stream()
                .filter(result -> result.latitude() != null && result.longitude() != null)
                .findFirst()
                .map(result -> new Location(result.name(), result.latitude(), result.longitude(), result.timezone()));
    }
}
