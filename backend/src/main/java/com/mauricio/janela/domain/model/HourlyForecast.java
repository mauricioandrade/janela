package com.mauricio.janela.domain.model;

import java.time.LocalDateTime;

/**
 * One forecast hour in the location's local time. Weather values are nullable: a missing value
 * means "unknown" and is treated as neutral (no penalty) by the scorer.
 */
public record HourlyForecast(
        LocalDateTime time,
        Double temperatureC,
        Double apparentTemperatureC,
        Integer precipitationProbability,
        Double uvIndex,
        Double windSpeedKmh,
        Boolean isDay
) {
}
