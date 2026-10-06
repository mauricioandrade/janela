package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Location;

/**
 * A city suggestion. {@code id} goes back to {@code GET /api/windows} as {@code cityId}.
 */
public record CityResponse(Long id, String name, String admin1, String country, String countryCode,
                           double latitude, double longitude) {

    public static CityResponse from(Location location) {
        return new CityResponse(location.id(), location.name(), location.admin1(), location.country(),
                location.countryCode(), location.latitude(), location.longitude());
    }
}
