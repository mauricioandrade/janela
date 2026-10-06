package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.exception.ExternalServiceUnavailableException;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

        assertThat(client.findByName("Campinas", Language.PT))
                .contains(new Location(3467865L, "Campinas", "São Paulo", "Brasil", "BR", -22.90556, -47.06083,
                        "America/Sao_Paulo"));
        server.verify();
    }

    @Test
    void searchesSuggestionsInTheRequestedLanguage() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andExpect(queryParam("name", "Campinas"))
                .andExpect(queryParam("count", "6"))
                .andExpect(queryParam("language", "en"))
                .andRespond(withSuccess(new ClassPathResource("openmeteo/geocoding-campinas.json"),
                        MediaType.APPLICATION_JSON));

        assertThat(client.search("Campinas", 6, Language.EN))
                .extracting(Location::id, Location::admin1, Location::countryCode)
                .containsExactly(tuple(3467865L, "São Paulo", "BR"));
        server.verify();
    }

    @Test
    void findsPlaceById() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/get")))
                .andExpect(queryParam("id", "3460543"))
                .andExpect(queryParam("language", "en"))
                .andRespond(withSuccess("""
                        {"id": 3460543, "name": "Itobi", "latitude": -21.73694, "longitude": -46.975,
                         "country_code": "BR", "timezone": "America/Sao_Paulo", "country": "Brasil",
                         "admin1": "São Paulo"}""", MediaType.APPLICATION_JSON));

        assertThat(client.findById(3460543, Language.EN))
                .contains(new Location(3460543L, "Itobi", "São Paulo", "Brasil", "BR", -21.73694, -46.975,
                        "America/Sao_Paulo"));
        server.verify();
    }

    @Test
    void returnsEmptyForUnknownId() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/get")))
                .andRespond(withBadRequest().body("{\"error\": true, \"reason\": \"Location ID not found.\"}"));

        assertThat(client.findById(99999999, Language.PT)).isEmpty();
    }

    @Test
    void returnsEmptyWhenNothingMatches() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andRespond(withSuccess("{\"generationtime_ms\": 0.4}", MediaType.APPLICATION_JSON));

        assertThat(client.findByName("Nowhereville", Language.PT)).isEmpty();
    }

    @Test
    void wrapsServerErrors() {
        server.expect(requestTo(startsWith("https://geocoding.test/v1/search")))
                .andRespond(withServiceUnavailable());

        assertThatThrownBy(() -> client.findByName("Campinas", Language.PT))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}
