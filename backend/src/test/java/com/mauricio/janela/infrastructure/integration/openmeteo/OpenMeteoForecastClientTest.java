package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoForecastClientTest {

    private static final Location CAMPINAS = new Location("Campinas", -22.9, -47.06, "America/Sao_Paulo");

    private MockRestServiceServer server;
    private OpenMeteoForecastClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://forecast.test/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenMeteoForecastClient(builder.build());
    }

    @Test
    void mapsParallelHourlyArraysByIndex() {
        server.expect(requestTo(startsWith("https://forecast.test/v1/forecast")))
                .andExpect(queryParam("latitude", "-22.9"))
                .andExpect(queryParam("longitude", "-47.06"))
                .andExpect(queryParam("hourly", OpenMeteoForecastClient.HOURLY_VARIABLES))
                .andExpect(queryParam("timezone", "auto"))
                .andExpect(queryParam("forecast_days", "2"))
                .andRespond(withSuccess(new ClassPathResource("openmeteo/forecast-campinas.json"),
                        MediaType.APPLICATION_JSON));

        List<HourlyForecast> hours = client.getHourlyForecast(CAMPINAS, 2);

        server.verify();
        assertThat(hours).hasSize(4);
        assertThat(hours.getFirst().isDay()).isFalse();
        assertThat(hours.get(2)).isEqualTo(new HourlyForecast(
                LocalDateTime.of(2026, 10, 6, 7, 0), 19.1, 19.4, 3, 1.2, 8.0, true));
    }

    @Test
    void keepsNullValuesInsteadOfFailing() {
        server.expect(requestTo(startsWith("https://forecast.test/v1/forecast")))
                .andRespond(withSuccess(new ClassPathResource("openmeteo/forecast-campinas.json"),
                        MediaType.APPLICATION_JSON));

        HourlyForecast lastHour = client.getHourlyForecast(CAMPINAS, 2).getLast();

        assertThat(lastHour.apparentTemperatureC()).isNull();
        assertThat(lastHour.temperatureC()).isEqualTo(21.0);
    }

    @Test
    void toleratesMissingOrShortArrays() {
        String body = """
                {"hourly": {"time": ["2026-10-06T06:00", "2026-10-06T07:00"], "uv_index": [0.4]}}
                """;
        server.expect(requestTo(startsWith("https://forecast.test/v1/forecast")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<HourlyForecast> hours = client.getHourlyForecast(CAMPINAS, 1);

        assertThat(hours).hasSize(2);
        assertThat(hours.getFirst().uvIndex()).isEqualTo(0.4);
        assertThat(hours.getLast()).isEqualTo(new HourlyForecast(
                LocalDateTime.of(2026, 10, 6, 7, 0), null, null, null, null, null, null));
    }

    @Test
    void wrapsServerErrors() {
        server.expect(requestTo(startsWith("https://forecast.test/v1/forecast")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getHourlyForecast(CAMPINAS, 1))
                .isInstanceOf(WeatherUnavailableException.class);
    }
}
