package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.port.in.SearchCitiesUseCase;
import com.mauricio.janela.infrastructure.web.request.CitiesRequest;
import com.mauricio.janela.infrastructure.web.response.CityResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CitiesController {

    private final SearchCitiesUseCase searchCitiesUseCase;

    public CitiesController(SearchCitiesUseCase searchCitiesUseCase) {
        this.searchCitiesUseCase = searchCitiesUseCase;
    }

    @GetMapping
    public List<CityResponse> searchCities(@Valid @ModelAttribute CitiesRequest request) {
        return searchCitiesUseCase.searchCities(request.q(), request.language()).stream()
                .map(CityResponse::from)
                .toList();
    }
}
