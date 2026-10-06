package com.mauricio.janela.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class OpenMeteoConfig {

    public static final String GEOCODING_CLIENT = "openMeteoGeocodingRestClient";
    public static final String FORECAST_CLIENT = "openMeteoForecastRestClient";

    @Bean(GEOCODING_CLIENT)
    RestClient openMeteoGeocodingRestClient(RestClient.Builder builder, OpenMeteoProperties properties) {
        return builder.clone()
                .baseUrl(properties.geocodingBaseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    @Bean(FORECAST_CLIENT)
    RestClient openMeteoForecastRestClient(RestClient.Builder builder, OpenMeteoProperties properties) {
        return builder.clone()
                .baseUrl(properties.forecastBaseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    private static ClientHttpRequestFactory requestFactory(OpenMeteoProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
