package com.mauricio.janela.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.time.LocalDateTime;

/**
 * A window in the location's local time; {@code end} is exclusive.
 */
public record WindowResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime start,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime end,
        int score,
        Double apparentTempC,
        Double maxUv,
        Integer maxRainProbability,
        Double maxWindKmh
) {

    public static WindowResponse from(OutdoorWindow window) {
        return new WindowResponse(window.start(), window.end(), window.score(), window.apparentTempC(),
                window.maxUv(), window.maxRainProbability(), window.maxWindKmh());
    }
}
