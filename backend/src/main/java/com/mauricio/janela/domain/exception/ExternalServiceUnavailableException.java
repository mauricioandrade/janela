package com.mauricio.janela.domain.exception;

/**
 * An external source the use cases depend on (forecast or geocoding) could not be reached or answered with an
 * error.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
