package com.mauricio.janela.domain.port.in;

public interface FindWindowsUseCase {

    /**
     * @throws com.mauricio.janela.domain.exception.LocationNotFoundException    if the city is unknown
     * @throws com.mauricio.janela.domain.exception.WeatherUnavailableException if the weather source fails
     */
    WindowsResult findWindows(FindWindowsQuery query);
}
