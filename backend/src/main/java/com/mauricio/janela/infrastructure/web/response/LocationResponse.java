package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Location;

public record LocationResponse(String name, double latitude, double longitude, String timezone) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(location.name(), location.latitude(), location.longitude(), location.timezone());
    }
}
