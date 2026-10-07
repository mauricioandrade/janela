package com.mauricio.janela.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The shape of one day's daylight: when it is hottest, wettest and sunniest, and when the light ends. It lets the narrative explain why a
 * window wins ("before the 13:00 heat") without the model reading raw hourly data. Any peak may be null.
 */
public record DayOutlook(
        LocalDate date,
        LocalDateTime hottestAt,
        Double hottestApparentTempC,
        LocalDateTime wettestAt,
        Integer maxRainProbability,
        LocalDateTime strongestSunAt,
        Double maxUv,
        LocalDateTime lastLightAt
) {

    /** An outlook without the end of daylight, for callers that only care about the peaks. */
    public DayOutlook(LocalDate date, LocalDateTime hottestAt, Double hottestApparentTempC, LocalDateTime wettestAt,
                      Integer maxRainProbability, LocalDateTime strongestSunAt, Double maxUv) {
        this(date, hottestAt, hottestApparentTempC, wettestAt, maxRainProbability, strongestSunAt, maxUv, null);
    }

    /** The outlook of the daylight hours in {@code hours} that fall on {@code date}. */
    public static DayOutlook of(LocalDate date, List<HourlyForecast> hours) {
        List<HourlyForecast> daylight = hours.stream()
                .filter(hour -> hour.time() != null && hour.time().toLocalDate().equals(date))
                .filter(hour -> !Boolean.FALSE.equals(hour.isDay()))
                .toList();
        Optional<HourlyForecast> hottest = peak(daylight, HourlyForecast::apparentTemperatureC);
        Optional<HourlyForecast> wettest = peak(daylight, hour -> hour.precipitationProbability() == null
                ? null : hour.precipitationProbability().doubleValue());
        Optional<HourlyForecast> sunniest = peak(daylight, HourlyForecast::uvIndex);
        return new DayOutlook(date,
                hottest.map(HourlyForecast::time).orElse(null),
                hottest.map(HourlyForecast::apparentTemperatureC).orElse(null),
                wettest.map(HourlyForecast::time).orElse(null),
                wettest.map(HourlyForecast::precipitationProbability).orElse(null),
                sunniest.map(HourlyForecast::time).orElse(null),
                sunniest.map(HourlyForecast::uvIndex).orElse(null),
                // The end of the last hour Open-Meteo still marks as day, about when the sun sets.
                daylight.stream().map(HourlyForecast::time).max(Comparator.naturalOrder())
                        .map(time -> time.plusHours(1)).orElse(null));
    }

    /** The first hour with the highest value; ties go to the earliest hour. */
    private static Optional<HourlyForecast> peak(List<HourlyForecast> hours, Function<HourlyForecast, Double> value) {
        return hours.stream()
                .filter(hour -> value.apply(hour) != null)
                .min(Comparator.comparing((HourlyForecast hour) -> -value.apply(hour))
                        .thenComparing(HourlyForecast::time));
    }
}
