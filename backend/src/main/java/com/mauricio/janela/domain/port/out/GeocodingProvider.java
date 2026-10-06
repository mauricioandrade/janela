package com.mauricio.janela.domain.port.out;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;

import java.util.List;
import java.util.Optional;

public interface GeocodingProvider {

    /** The best match for a free-text city name. */
    Optional<Location> findByName(String city);

    /** The exact place behind a geocoder id, as returned by {@link #search}. */
    Optional<Location> findById(long id);

    /** Up to {@code limit} places matching a partial name, best first, with names in the given language. */
    List<Location> search(String query, int limit, Language language);
}
