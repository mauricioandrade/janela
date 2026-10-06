package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mauricio.janela.domain.model.Location;

import java.util.List;

/**
 * Open-Meteo geocoding response. {@code results} is absent when nothing matches.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Result> results) {

    /** One place; {@code /search} returns a list of these and {@code /get} returns a single one. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Long id,
            String name,
            String admin1,
            String country,
            @JsonProperty("country_code") String countryCode,
            Double latitude,
            Double longitude,
            String timezone
    ) {

        boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }

        Location toLocation() {
            return new Location(id, name, admin1, country, countryCode, latitude, longitude, timezone);
        }
    }
}
