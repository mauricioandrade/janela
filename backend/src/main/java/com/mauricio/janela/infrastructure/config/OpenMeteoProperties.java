package com.mauricio.janela.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("janela.open-meteo")
public record OpenMeteoProperties(
        @NotBlank String geocodingBaseUrl,
        @NotBlank String forecastBaseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout
) {
}
