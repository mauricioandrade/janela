package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.port.in.FindWindowsUseCase;
import com.mauricio.janela.infrastructure.web.request.WindowsRequest;
import com.mauricio.janela.infrastructure.web.response.WindowsResponse;
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

@RestController
@RequestMapping("/api/{version}/windows")
@Tag(name = "Windows", description = "Best outdoor time windows and the model's recommendation")
public class WindowsController {

    private final FindWindowsUseCase findWindowsUseCase;

    public WindowsController(FindWindowsUseCase findWindowsUseCase) {
        this.findWindowsUseCase = findWindowsUseCase;
    }

    @GetMapping(version = "v1")
    @Operation(
            summary = "Find the best windows for an activity",
            description = """
                    Scores the hourly forecast in deterministic Java and returns the best non-overlapping daytime \
                    windows, best first and the same number per day (3 for one day, 2 a day for two, 1 a day for \
                    three), plus a short recommendation. The narrative is written by the local \
                    model (aiGenerated=true) or, if it is unavailable, by a template (aiGenerated=false).""")
    @ApiResponse(responseCode = "200", description = "Windows found (the list may be empty)")
    @ApiResponse(responseCode = "400", description = "Invalid parameters; `fields` lists the offending ones",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ApiErrorResponses.Problem.class),
                    examples = @ExampleObject(value = ApiErrorResponses.INVALID_PARAMETERS)))
    @ApiResponse(responseCode = "404", description = "No city matches the name or cityId",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ApiErrorResponses.Problem.class),
                    examples = @ExampleObject(value = ApiErrorResponses.CITY_NOT_FOUND)))
    @ApiResponse(responseCode = "503", description = "Open-Meteo could not be reached",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ApiErrorResponses.Problem.class),
                    examples = @ExampleObject(value = ApiErrorResponses.WEATHER_UNAVAILABLE)))
    public WindowsResponse findWindows(@ParameterObject @Valid @ModelAttribute WindowsRequest request) {
        return WindowsResponse.from(findWindowsUseCase.findWindows(request.toQuery()));
    }
}
