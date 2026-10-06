package com.mauricio.janela.domain.port.in;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;

/**
 * {@code cityId}, when present, pins the exact place picked from the city suggestions; {@code city} is then
 * only a label. Without it, the city name is geocoded and the best match wins.
 */
public record FindWindowsQuery(String city, Long cityId, Activity activity, int durationMinutes, int days,
                               Language language) {
}
