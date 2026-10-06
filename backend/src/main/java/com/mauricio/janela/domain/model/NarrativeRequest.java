package com.mauricio.janela.domain.model;

import java.util.List;

/**
 * Everything a narrative generator may talk about. Windows are already ranked, best first.
 */
public record NarrativeRequest(
        Location location,
        Activity activity,
        int durationMinutes,
        List<OutdoorWindow> windows,
        Language language
) {

    public NarrativeRequest {
        windows = List.copyOf(windows);
    }
}
