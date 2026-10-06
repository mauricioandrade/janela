package com.mauricio.janela.domain.port.out;

import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.Location;

import java.util.List;

public interface WeatherProvider {

    /**
     * Hourly forecast for {@code days} days starting today, in the location's local time.
     */
    List<HourlyForecast> getHourlyForecast(Location location, int days);
}
