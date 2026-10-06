package com.mauricio.janela.infrastructure.web.request;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.port.in.FindWindowsQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of {@code GET /api/v1/windows}. {@code days} defaults to 1 and {@code lang} to "pt".
 * {@code cityId} comes from {@code GET /api/v1/cities} and pins the exact place; Open-Meteo ids are 32-bit.
 */
public record WindowsRequest(
        @Schema(description = "City name; a label only when cityId is given", example = "Itobi")
        @NotBlank @Size(max = 100) String city,
        @Schema(description = "Place id from GET /api/v1/cities; pins that exact place", example = "3460543")
        @Positive @Max(Integer.MAX_VALUE) Long cityId,
        @Schema(description = "Outdoor activity; each has its own comfort limits", example = "BIKE")
        @NotNull Activity activity,
        @Schema(description = "How long the activity takes, rounded up to whole hours", example = "120",
                minimum = "15", maximum = "480")
        @NotNull @Min(15) @Max(480) Integer durationMinutes,
        @Schema(description = "Days to look ahead, starting today", example = "2", minimum = "1", maximum = "3",
                defaultValue = "1")
        @Min(1) @Max(3) Integer days,
        @Schema(description = "Language of the narrative", allowableValues = {"pt", "en"}, defaultValue = "pt")
        @Pattern(regexp = "pt|en") String lang
) {

    public FindWindowsQuery toQuery() {
        return new FindWindowsQuery(
                city.strip(),
                cityId,
                activity,
                durationMinutes,
                days == null ? 1 : days,
                lang == null ? Language.PT : Language.fromCode(lang));
    }
}
