package com.mauricio.janela.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mauricio.janela.domain.model.HourScore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record HourScoreResponse(
        @Schema(type = "string", description = "Local start of the hour", example = "2026-10-08T06:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime time,
        @Schema(description = "Comfort score of this hour, 0–100", example = "97", minimum = "0", maximum = "100")
        int score
) {

    public static HourScoreResponse from(HourScore hour) {
        return new HourScoreResponse(hour.time(), hour.score());
    }
}
