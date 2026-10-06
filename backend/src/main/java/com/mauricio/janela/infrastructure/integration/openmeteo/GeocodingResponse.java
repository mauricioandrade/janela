package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Open-Meteo geocoding response. {@code results} is absent when nothing matches.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(String name, Double latitude, Double longitude, String timezone) {
    }
}
