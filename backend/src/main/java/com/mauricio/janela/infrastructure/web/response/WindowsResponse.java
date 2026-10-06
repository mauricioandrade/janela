package com.mauricio.janela.infrastructure.web.response;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.port.in.WindowsResult;

import java.util.List;

public record WindowsResponse(
        LocationResponse location,
        Activity activity,
        List<WindowResponse> windows,
        String narrative,
        boolean aiGenerated,
        String model
) {

    public static WindowsResponse from(WindowsResult result) {
        return new WindowsResponse(
                LocationResponse.from(result.location()),
                result.activity(),
                result.windows().stream().map(WindowResponse::from).toList(),
                result.narrative().text(),
                result.narrative().aiGenerated(),
                result.narrative().model());
    }
}
