package com.mauricio.janela.domain.port.out;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;

import java.util.List;
import java.util.Optional;

public interface GeocodingProvider {

    /** The best match for a free-text city name, with names in the given language. */
    Optional<Location> findByName(String city, Language language);

    /** The exact place behind a geocoder id, as returned by {@link #search}, with names in the given language. */
    Optional<Location> findById(long id, Language language);

    /** Up to {@code limit} places matching a partial name, best first, with names in the given language. */
    List<Location> search(String query, int limit, Language language);
}
