package com.mauricio.janela.domain.port.in;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;

import java.util.List;

public interface SearchCitiesUseCase {

    /** City suggestions for a partial name, best first. */
    List<Location> searchCities(String query, Language language);
}
