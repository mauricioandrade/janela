package com.mauricio.janela.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Everything a narrative generator may talk about. Windows are already ranked, best first; {@code outlooks} describe
 * each considered day, in date order.
 */
public record NarrativeRequest(
        Location location,
        Activity activity,
        int durationMinutes,
        List<OutdoorWindow> windows,
        Language language,
        List<DayOutlook> outlooks
) {

    public NarrativeRequest {
        windows = List.copyOf(windows);
        outlooks = List.copyOf(outlooks);
    }

    /** The outlook of the day a window starts on, if that day was considered. */
    public Optional<DayOutlook> outlookFor(OutdoorWindow window) {
        return outlooks.stream().filter(outlook -> outlook.date().equals(window.start().toLocalDate())).findFirst();
    }
}
