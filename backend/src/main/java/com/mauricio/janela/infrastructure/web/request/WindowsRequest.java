package com.mauricio.janela.infrastructure.web.request;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.port.in.FindWindowsQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of {@code GET /api/windows}. {@code days} defaults to 1 and {@code lang} to "pt".
 * {@code cityId} comes from {@code GET /api/cities} and pins the exact place; Open-Meteo ids are 32-bit.
 */
public record WindowsRequest(
        @NotBlank @Size(max = 100) String city,
        @Positive @Max(Integer.MAX_VALUE) Long cityId,
        @NotNull Activity activity,
        @NotNull @Min(15) @Max(480) Integer durationMinutes,
        @Min(1) @Max(3) Integer days,
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
