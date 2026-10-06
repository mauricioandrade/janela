package com.mauricio.janela.domain.model;

/**
 * A geocoded place. {@code id} is the geocoder's identifier, used to pick the exact place a person chose;
 * {@code admin1} is the state or province. Those and the country fields may be null.
 */
public record Location(
        Long id,
        String name,
        String admin1,
        String country,
        String countryCode,
        double latitude,
        double longitude,
        String timezone
) {
}
