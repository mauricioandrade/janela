package com.mauricio.janela.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * In-memory caches for Open-Meteo answers: places hardly change, forecasts refresh hourly. Besides speed, this
 * keeps the city suggestions from hitting Open-Meteo on every keystroke that repeats a query.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PLACES = "places";
    public static final String FORECASTS = "forecasts";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(PLACES,
                Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(24)).maximumSize(5_000).build());
        manager.registerCustomCache(FORECASTS,
                Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(15)).maximumSize(500).build());
        return manager;
    }
}
