package com.mauricio.janela.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Example problem-detail bodies shared by the OpenAPI annotations of the controllers.
 */
final class ApiErrorResponses {

    /**
     * Documentation-only shape of the RFC 9457 bodies: Spring's ProblemDetail flattens extra properties such as
     * {@code fields} into the root, which its own generated schema doesn't show.
     */
    @Schema(name = "Problem", description = "RFC 9457 problem details")
    record Problem(
            @Schema(example = "about:blank") String type,
            @Schema(example = "Bad Request") String title,
            @Schema(example = "400") int status,
            @Schema(example = "Invalid request parameters.") String detail,
            @Schema(example = "/api/windows") String instance,
            @Schema(description = "Invalid parameters; only on 400", nullable = true) List<String> fields
    ) {
    }

    static final String INVALID_PARAMETERS = """
            {"type": "about:blank", "title": "Bad Request", "status": 400,
             "detail": "Invalid request parameters.", "instance": "/api/windows", "fields": ["durationMinutes"]}""";

    static final String CITY_NOT_FOUND = """
            {"type": "about:blank", "title": "Not Found", "status": 404,
             "detail": "City not found.", "instance": "/api/windows"}""";

    static final String WEATHER_UNAVAILABLE = """
            {"type": "about:blank", "title": "Service Unavailable", "status": 503,
             "detail": "Weather data is temporarily unavailable. Please try again later.",
             "instance": "/api/windows"}""";

    private ApiErrorResponses() {
    }
}
