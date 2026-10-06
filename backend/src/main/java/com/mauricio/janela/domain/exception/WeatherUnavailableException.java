package com.mauricio.janela.domain.exception;

/**
 * The weather or geocoding source could not be reached or answered with an error.
 */
public class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
