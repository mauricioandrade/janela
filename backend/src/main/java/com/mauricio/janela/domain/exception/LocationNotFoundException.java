package com.mauricio.janela.domain.exception;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String city) {
        super("Location not found: " + city);
    }
}
