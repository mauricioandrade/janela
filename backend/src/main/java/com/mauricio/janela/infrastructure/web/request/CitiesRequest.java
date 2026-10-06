package com.mauricio.janela.infrastructure.web.request;

import com.mauricio.janela.domain.model.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of {@code GET /api/cities}. {@code lang} defaults to "pt" and sets the language of place names.
 */
public record CitiesRequest(
        @NotBlank @Size(min = 2, max = 100) String q,
        @Pattern(regexp = "pt|en") String lang
) {

    public Language language() {
        return lang == null ? Language.PT : Language.fromCode(lang);
    }
}
