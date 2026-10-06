package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.port.in.SearchCitiesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitiesController.class)
class CitiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchCitiesUseCase searchCitiesUseCase;

    @Test
    void returnsSuggestionsWithStateAndCountry() throws Exception {
        when(searchCitiesUseCase.searchCities("Itobi", Language.EN)).thenReturn(List.of(new Location(
                3460543L, "Itobi", "São Paulo", "Brasil", "BR", -21.73694, -46.975, "America/Sao_Paulo")));

        mockMvc.perform(get("/api/cities").param("q", "Itobi").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3460543))
                .andExpect(jsonPath("$[0].name").value("Itobi"))
                .andExpect(jsonPath("$[0].admin1").value("São Paulo"))
                .andExpect(jsonPath("$[0].countryCode").value("BR"));
    }

    @Test
    void rejectsQueriesShorterThanTwoCharacters() throws Exception {
        mockMvc.perform(get("/api/cities").param("q", "I"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[0]").value("q"));

        verifyNoInteractions(searchCitiesUseCase);
    }
}
