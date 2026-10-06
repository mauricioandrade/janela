package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.model.HourlyForecast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps Open-Meteo's parallel hourly arrays to domain hours by index. Missing arrays or values become null.
 */
final class ForecastMapper {

    private ForecastMapper() {
    }

    static List<HourlyForecast> toHourlyForecasts(ForecastResponse response) {
        if (response == null || response.hourly() == null || response.hourly().time() == null) {
            return List.of();
        }
        ForecastResponse.Hourly hourly = response.hourly();
        List<HourlyForecast> hours = new ArrayList<>(hourly.time().size());

        for (int i = 0; i < hourly.time().size(); i++) {
            String time = hourly.time().get(i);
            if (time == null) {
                continue;
            }
            Integer isDay = valueAt(hourly.isDay(), i);
            hours.add(new HourlyForecast(
                    LocalDateTime.parse(time),
                    valueAt(hourly.temperature2m(), i),
                    valueAt(hourly.apparentTemperature(), i),
                    valueAt(hourly.precipitationProbability(), i),
                    valueAt(hourly.uvIndex(), i),
                    valueAt(hourly.windSpeed10m(), i),
                    isDay == null ? null : isDay != 0));
        }
        return List.copyOf(hours);
    }

    private static <T> T valueAt(List<T> values, int index) {
        return values != null && index < values.size() ? values.get(index) : null;
    }
}
