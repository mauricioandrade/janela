package com.mauricio.janela.infrastructure.web.request;

import com.mauricio.janela.domain.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of {@code GET /api/cities}. {@code lang} defaults to "pt" and sets the language of place names.
 */
public record CitiesRequest(
        @Schema(description = "Partial city name", example = "Itobi", minLength = 2, maxLength = 100)
        @NotBlank @Size(min = 2, max = 100) String q,
        @Schema(description = "Language of place and country names", allowableValues = {"pt", "en"},
                defaultValue = "pt")
        @Pattern(regexp = "pt|en") String lang
) {

    public Language language() {
        return lang == null ? Language.PT : Language.fromCode(lang);
    }
}
