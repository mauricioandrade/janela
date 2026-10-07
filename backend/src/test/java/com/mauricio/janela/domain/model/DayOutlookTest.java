package com.mauricio.janela.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DayOutlookTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 7);

    @Test
    void findsTheHottestRainiestAndSunniestDaylightHours() {
        List<HourlyForecast> hours = List.of(
                hour(DAY, 7, 24.0, 0, 1.0, true),
                hour(DAY, 12, 33.0, 10, 11.0, true),
                hour(DAY, 13, 34.0, 20, 11.0, true),
                hour(DAY, 16, 30.0, 70, 4.0, true),
                hour(DAY, 21, 40.0, 90, 0.0, false),        // night: ignored
                hour(DAY.plusDays(1), 13, 38.0, 99, 12.0, true)); // another day: ignored

        DayOutlook outlook = DayOutlook.of(DAY, hours);

        assertThat(outlook.hottestAt()).isEqualTo(DAY.atTime(13, 0));
        assertThat(outlook.hottestApparentTempC()).isEqualTo(34.0);
        assertThat(outlook.wettestAt()).isEqualTo(DAY.atTime(16, 0));
        assertThat(outlook.maxRainProbability()).isEqualTo(70);
        assertThat(outlook.strongestSunAt()).isEqualTo(DAY.atTime(12, 0)); // ties go to the earliest hour
        assertThat(outlook.maxUv()).isEqualTo(11.0);
    }

    @Test
    void leavesPeaksEmptyWithoutData() {
        DayOutlook outlook = DayOutlook.of(DAY, List.of(hour(DAY, 8, null, null, null, true)));

        assertThat(outlook.hottestAt()).isNull();
        assertThat(outlook.wettestAt()).isNull();
        assertThat(outlook.strongestSunAt()).isNull();
    }

    private static HourlyForecast hour(LocalDate day, int hour, Double feelsLike, Integer rain, Double uv, boolean isDay) {
        return new HourlyForecast(day.atTime(hour, 0), feelsLike, feelsLike, rain, uv, 5.0, isDay);
    }
}
