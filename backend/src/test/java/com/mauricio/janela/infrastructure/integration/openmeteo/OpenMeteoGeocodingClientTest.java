package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import com.mauricio.janela.domain.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpMethod.GET;

class OpenMeteoGeocodingClientTest {

    private MockRestServiceServer server;
    private OpenMeteoGeocodingClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://geocoding.test/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenMeteoGeocodingClient(builder.build());
    }

    @Test
    void mapsFirstResultToLocation() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andExpect(method(GET))
                .andExpect(queryParam("name", "Campinas"))
                .andExpect(queryParam("count", "1"))
                .andExpect(queryParam("language", "pt"))
                .andRespond(withSuccess(new ClassPathResource("openmeteo/geocoding-campinas.json"),
                        MediaType.APPLICATION_JSON));

        assertThat(client.findByName("Campinas"))
                .contains(new Location("Campinas", -22.90556, -47.06083, "America/Sao_Paulo"));
        server.verify();
    }

    @Test
    void returnsEmptyWhenNothingMatches() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andRespond(withSuccess("{\"generationtime_ms\": 0.4}", MediaType.APPLICATION_JSON));

        assertThat(client.findByName("Nowhereville")).isEmpty();
    }

    @Test
    void wrapsServerErrors() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andRespond(withServiceUnavailable());

        assertThatThrownBy(() -> client.findByName("Campinas"))
                .isInstanceOf(WeatherUnavailableException.class);
    }
}
