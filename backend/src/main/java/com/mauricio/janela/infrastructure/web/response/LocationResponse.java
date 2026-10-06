package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The place the search resolved to. {@code id} is null when the geocoder gave none.
 */
public record LocationResponse(
        @Schema(example = "3460543", nullable = true) Long id,
        @Schema(example = "Itobi") String name,
        @Schema(description = "State or province", example = "São Paulo", nullable = true) String admin1,
        @Schema(example = "Brasil", nullable = true) String country,
        @Schema(description = "ISO 3166-1 alpha-2", example = "BR", nullable = true) String countryCode,
        @Schema(example = "-21.73694") double latitude,
        @Schema(example = "-46.975") double longitude,
        @Schema(description = "IANA time zone; window times are local to it", example = "America/Sao_Paulo")
        String timezone
) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(location.id(), location.name(), location.admin1(), location.country(),
                location.countryCode(), location.latitude(), location.longitude(), location.timezone());
    }
}
