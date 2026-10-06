package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Open-Meteo forecast response. Every list in {@link Hourly} is parallel to {@code time}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponse(String timezone, Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperature2m,
            @JsonProperty("apparent_temperature") List<Double> apparentTemperature,
            @JsonProperty("precipitation_probability") List<Integer> precipitationProbability,
            @JsonProperty("uv_index") List<Double> uvIndex,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed10m,
            @JsonProperty("is_day") List<Integer> isDay
    ) {
    }
}
