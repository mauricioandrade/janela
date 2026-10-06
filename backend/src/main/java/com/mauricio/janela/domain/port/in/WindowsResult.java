package com.mauricio.janela.domain.port.in;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.util.List;

public record WindowsResult(Location location, Activity activity, List<OutdoorWindow> windows, Narrative narrative) {

    public WindowsResult {
        windows = List.copyOf(windows);
    }
}
