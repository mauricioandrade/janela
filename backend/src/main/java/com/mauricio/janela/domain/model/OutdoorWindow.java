package com.mauricio.janela.domain.model;

import java.time.LocalDateTime;

/**
 * A ranked block of consecutive hours. {@code end} is exclusive (06:00–07:00 is a one-hour window).
 * Aggregated values are null when the forecast had no data for any hour of the window.
 */
public record OutdoorWindow(
        LocalDateTime start,
        LocalDateTime end,
        int score,
        Double apparentTempC,
        Double maxUv,
        Integer maxRainProbability,
        Double maxWindKmh
) {

    public boolean overlaps(OutdoorWindow other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
