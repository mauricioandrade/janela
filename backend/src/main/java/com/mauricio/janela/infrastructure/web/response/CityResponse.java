package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A city suggestion. {@code id} goes back to {@code GET /api/windows} as {@code cityId}.
 */
public record CityResponse(
        @Schema(description = "Place id; send it back as cityId", example = "3460543") Long id,
        @Schema(example = "Itobi") String name,
        @Schema(description = "State or province", example = "São Paulo", nullable = true) String admin1,
        @Schema(example = "Brasil", nullable = true) String country,
        @Schema(description = "ISO 3166-1 alpha-2", example = "BR", nullable = true) String countryCode,
        @Schema(example = "-21.73694") double latitude,
        @Schema(example = "-46.975") double longitude
) {

    public static CityResponse from(Location location) {
        return new CityResponse(location.id(), location.name(), location.admin1(), location.country(),
                location.countryCode(), location.latitude(), location.longitude());
    }
}
