package com.mauricio.janela.domain.port.in;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.HourScore;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.util.List;

/**
 * {@code hours} scores every daylight hour that was considered, so the windows can be shown in context.
 */
public record WindowsResult(Location location, Activity activity, List<OutdoorWindow> windows, Narrative narrative,
                            List<HourScore> hours) {

    public WindowsResult {
        windows = List.copyOf(windows);
        hours = List.copyOf(hours);
    }
}
