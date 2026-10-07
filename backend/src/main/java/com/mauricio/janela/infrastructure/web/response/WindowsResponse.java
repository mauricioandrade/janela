package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.port.in.WindowsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record WindowsResponse(
        LocationResponse location,
        @Schema(example = "BIKE") Activity activity,
        @Schema(description = "Up to 3 non-overlapping windows, best first; empty when no hour is good enough")
        List<WindowResponse> windows,
        @Schema(description = "Short recommendation ending with a \"🌿\" touch-grass challenge",
                example = "A melhor opção para pedalar é quarta-feira, 07/10, das 07:00 às 08:00…")
        String narrative,
        @Schema(description = "true when the local model wrote the narrative, false for the template")
        boolean aiGenerated,
        @Schema(description = "Model that wrote the narrative; null for the template", example = "gemma3:4b",
                nullable = true)
        String model,
        @Schema(description = "Comfort score of every daylight hour considered, in time order")
        List<HourScoreResponse> hours
) {

    public static WindowsResponse from(WindowsResult result) {
        return new WindowsResponse(
                LocationResponse.from(result.location()),
                result.activity(),
                result.windows().stream().map(WindowResponse::from).toList(),
                result.narrative().text(),
                result.narrative().aiGenerated(),
                result.narrative().model(),
                result.hours().stream().map(HourScoreResponse::from).toList());
    }
}
