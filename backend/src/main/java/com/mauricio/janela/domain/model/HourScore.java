package com.mauricio.janela.domain.model;

import java.time.LocalDateTime;

/**
 * The comfort score (0–100) of one daylight hour, in the location's local time.
 */
public record HourScore(LocalDateTime time, int score) {
}
