package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.ExternalServiceUnavailableException;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.out.WeatherProvider;
import com.mauricio.janela.infrastructure.config.OpenMeteoConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class OpenMeteoForecastClient implements WeatherProvider {

    static final String HOURLY_VARIABLES =
            "temperature_2m,apparent_temperature,precipitation_probability,uv_index,wind_speed_10m,is_day";

    private final RestClient restClient;

    public OpenMeteoForecastClient(@Qualifier(OpenMeteoConfig.FORECAST_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<HourlyForecast> getHourlyForecast(Location location, int days) {
        ForecastResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/forecast")
                            .queryParam("latitude", location.latitude())
                            .queryParam("longitude", location.longitude())
                            .queryParam("hourly", HOURLY_VARIABLES)
                            .queryParam("timezone", "auto")
                            .queryParam("forecast_days", days)
                            .build())
                    .retrieve()
                    .body(ForecastResponse.class);
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("Open-Meteo forecast request failed", e);
        }
        return ForecastMapper.toHourlyForecasts(response);
    }
}
