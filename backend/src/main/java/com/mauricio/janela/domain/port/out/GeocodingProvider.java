package com.mauricio.janela.domain.port.out;

import com.mauricio.janela.domain.model.Location;

import java.util.Optional;

public interface GeocodingProvider {

    Optional<Location> findByName(String city);
}
