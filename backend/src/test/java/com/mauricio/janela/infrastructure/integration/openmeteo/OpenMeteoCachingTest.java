package com.mauricio.janela.infrastructure.integration.openmeteo;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.infrastructure.config.CacheConfig;
import com.mauricio.janela.infrastructure.config.OpenMeteoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig(OpenMeteoCachingTest.TestConfig.class)
class OpenMeteoCachingTest {

    @Autowired
    private GeocodingProvider client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void repeatedSearchesHitOpenMeteoOnce() {
        server.expect(once(), requestTo(startsWith("https://geocoding.test/v1/search")))
                .andRespond(withSuccess(new ClassPathResource("openmeteo/geocoding-campinas.json"),
                        MediaType.APPLICATION_JSON));

        assertThat(client.search("Campinas", 6, Language.PT)).hasSize(1);
        assertThat(client.search("campinas", 6, Language.PT)).hasSize(1);
        server.verify();
    }

    @Configuration
    @Import({CacheConfig.class, OpenMeteoGeocodingClient.class})
    static class TestConfig {

        private final RestClient.Builder builder = RestClient.builder().baseUrl("https://geocoding.test/v1");

        @Bean
        MockRestServiceServer server() {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean(OpenMeteoConfig.GEOCODING_CLIENT)
        RestClient geocodingRestClient(MockRestServiceServer server) {
            return builder.build();
        }
    }
}
