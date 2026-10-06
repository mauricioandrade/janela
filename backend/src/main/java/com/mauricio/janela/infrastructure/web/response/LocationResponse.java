package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Location;

public record LocationResponse(Long id, String name, String admin1, String country, String countryCode,
                               double latitude, double longitude, String timezone) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(location.id(), location.name(), location.admin1(), location.country(),
                location.countryCode(), location.latitude(), location.longitude(), location.timezone());
    }
}
