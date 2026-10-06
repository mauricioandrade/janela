package com.mauricio.janela.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mauricio.janela.domain.model.OutdoorWindow;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * A window in the location's local time; {@code end} is exclusive.
 */
public record WindowResponse(
        @Schema(type = "string", description = "Local start time", example = "2026-10-07T07:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime start,
        @Schema(type = "string", description = "Local end time, exclusive", example = "2026-10-07T08:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime end,
        @Schema(description = "Average hour score, 0–100", example = "94", minimum = "0", maximum = "100")
        int score,
        @Schema(description = "Average feels-like temperature, °C", example = "23.1", nullable = true)
        Double apparentTempC,
        @Schema(description = "Highest UV index in the window", example = "0.4", nullable = true)
        Double maxUv,
        @Schema(description = "Highest chance of rain, %", example = "10", nullable = true)
        Integer maxRainProbability,
        @Schema(description = "Highest wind speed, km/h", example = "8.2", nullable = true)
        Double maxWindKmh
) {

    public static WindowResponse from(OutdoorWindow window) {
        return new WindowResponse(window.start(), window.end(), window.score(), window.apparentTempC(),
                window.maxUv(), window.maxRainProbability(), window.maxWindKmh());
    }
}
