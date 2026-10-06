package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.port.in.SearchCitiesUseCase;
import com.mauricio.janela.infrastructure.web.request.CitiesRequest;
import com.mauricio.janela.infrastructure.web.response.CityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@Tag(name = "Cities", description = "City suggestions for the search field")
public class CitiesController {

    private final SearchCitiesUseCase searchCitiesUseCase;

    public CitiesController(SearchCitiesUseCase searchCitiesUseCase) {
        this.searchCitiesUseCase = searchCitiesUseCase;
    }

    @GetMapping
    @Operation(
            summary = "Suggest cities for a partial name",
            description = """
                    Up to 6 places, best first, with state and country so namesakes can be told apart. \
                    Pass the chosen `id` to GET /api/windows as `cityId`.""")
    @ApiResponse(responseCode = "200", description = "Suggestions (empty when nothing matches)")
    @ApiResponse(responseCode = "400", description = "`q` is missing, shorter than 2 or longer than 100 characters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ApiErrorResponses.Problem.class),
                    examples = @ExampleObject(value = """
                            {"type": "about:blank", "title": "Bad Request", "status": 400,
                             "detail": "Invalid request parameters.", "instance": "/api/cities", "fields": ["q"]}""")))
    @ApiResponse(responseCode = "503", description = "Open-Meteo geocoding could not be reached",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ApiErrorResponses.Problem.class)))
    public List<CityResponse> searchCities(@ParameterObject @Valid @ModelAttribute CitiesRequest request) {
        return searchCitiesUseCase.searchCities(request.q(), request.language()).stream()
                .map(CityResponse::from)
                .toList();
    }
}
