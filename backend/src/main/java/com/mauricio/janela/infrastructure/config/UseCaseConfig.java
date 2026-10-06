package com.mauricio.janela.infrastructure.config;

import com.mauricio.janela.application.usecase.FindWindowsService;
import com.mauricio.janela.domain.port.in.FindWindowsUseCase;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.domain.port.out.WeatherProvider;
import com.mauricio.janela.domain.service.WindowScorer;
import com.mauricio.janela.infrastructure.ai.OllamaNarrativeGenerator;
import com.mauricio.janela.infrastructure.ai.TemplateNarrativeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the framework-free domain and application layers. The AI-or-template choice is made here, explicitly.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    WindowScorer windowScorer() {
        return new WindowScorer();
    }

    @Bean
    FindWindowsUseCase findWindowsUseCase(
            GeocodingProvider geocodingProvider,
            WeatherProvider weatherProvider,
            WindowScorer windowScorer,
            OllamaNarrativeGenerator ollamaNarrativeGenerator,
            TemplateNarrativeGenerator templateNarrativeGenerator,
            Clock clock) {
        return new FindWindowsService(geocodingProvider, weatherProvider, windowScorer,
                ollamaNarrativeGenerator, templateNarrativeGenerator, clock);
    }
}
