package com.mauricio.janela.domain.port.in;

public interface FindWindowsUseCase {

    /**
     * @throws com.mauricio.janela.domain.exception.LocationNotFoundException    if the city is unknown
     * @throws com.mauricio.janela.domain.exception.ExternalServiceUnavailableException if the forecast or geocoding source fails
     */
    WindowsResult findWindows(FindWindowsQuery query);
}
