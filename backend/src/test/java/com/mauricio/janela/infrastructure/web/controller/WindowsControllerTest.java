package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.exception.LocationNotFoundException;
import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.port.in.FindWindowsQuery;
import com.mauricio.janela.domain.port.in.FindWindowsUseCase;
import com.mauricio.janela.domain.port.in.WindowsResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WindowsController.class)
class WindowsControllerTest {

    private static final Location CAMPINAS = new Location("Campinas", -22.9, -47.06, "America/Sao_Paulo");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindWindowsUseCase findWindowsUseCase;

    @Test
    void returnsWindowsWithNarrative() throws Exception {
        OutdoorWindow window = new OutdoorWindow(
                LocalDateTime.of(2026, 10, 6, 6, 0), LocalDateTime.of(2026, 10, 6, 7, 0), 91, 19.4, 1.2, 5, 8.0);
        when(findWindowsUseCase.findWindows(any())).thenReturn(new WindowsResult(
                CAMPINAS, Activity.RUN, List.of(window), Narrative.fromModel("Go early.", "gemma3:4b")));

        mockMvc.perform(get("/api/windows")
                        .param("city", " Campinas ")
                        .param("activity", "RUN")
                        .param("durationMinutes", "60")
                        .param("days", "2")
                        .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.name").value("Campinas"))
                .andExpect(jsonPath("$.location.timezone").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.activity").value("RUN"))
                .andExpect(jsonPath("$.windows[0].start").value("2026-10-06T06:00"))
                .andExpect(jsonPath("$.windows[0].end").value("2026-10-06T07:00"))
                .andExpect(jsonPath("$.windows[0].score").value(91))
                .andExpect(jsonPath("$.windows[0].maxRainProbability").value(5))
                .andExpect(jsonPath("$.narrative").value("Go early."))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.model").value("gemma3:4b"));

        verify(findWindowsUseCase).findWindows(new FindWindowsQuery("Campinas", Activity.RUN, 60, 2, Language.EN));
    }

    @Test
    void defaultsToOneDayInPortuguese() throws Exception {
        when(findWindowsUseCase.findWindows(any())).thenReturn(new WindowsResult(
                CAMPINAS, Activity.WALK, List.of(), Narrative.fromTemplate("Sem janelas.")));

        mockMvc.perform(get("/api/windows")
                        .param("city", "Campinas")
                        .param("activity", "WALK")
                        .param("durationMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").value(false))
                .andExpect(jsonPath("$.model").doesNotExist());

        verify(findWindowsUseCase).findWindows(new FindWindowsQuery("Campinas", Activity.WALK, 30, 1, Language.PT));
    }

    @Test
    void rejectsOutOfRangeParameters() throws Exception {
        mockMvc.perform(get("/api/windows")
                        .param("city", "")
                        .param("activity", "RUN")
                        .param("durationMinutes", "5")
                        .param("days", "4")
                        .param("lang", "es"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request parameters."))
                .andExpect(jsonPath("$.fields", contains("city", "days", "durationMinutes", "lang")));

        verifyNoInteractions(findWindowsUseCase);
    }

    @Test
    void rejectsUnknownActivity() throws Exception {
        mockMvc.perform(get("/api/windows")
                        .param("city", "Campinas")
                        .param("activity", "SWIM")
                        .param("durationMinutes", "60"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields", contains("activity")));

        verifyNoInteractions(findWindowsUseCase);
    }

    @Test
    void returns404WhenCityIsUnknown() throws Exception {
        when(findWindowsUseCase.findWindows(any())).thenThrow(new LocationNotFoundException("Atlantis"));

        mockMvc.perform(get("/api/windows")
                        .param("city", "Atlantis")
                        .param("activity", "RUN")
                        .param("durationMinutes", "60"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("City not found."));
    }

    @Test
    void returns503WhenWeatherIsUnavailable() throws Exception {
        when(findWindowsUseCase.findWindows(any()))
                .thenThrow(new WeatherUnavailableException("Open-Meteo forecast request failed", null));

        mockMvc.perform(get("/api/windows")
                        .param("city", "Campinas")
                        .param("activity", "RUN")
                        .param("durationMinutes", "60"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Weather data is temporarily unavailable. Please try again later."));
    }

    @Test
    void allowsCorsOnlyFromTheFrontendDevServer() throws Exception {
        mockMvc.perform(options("/api/windows")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/api/windows")
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
