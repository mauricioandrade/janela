package com.mauricio.janela.application.usecase;

import com.mauricio.janela.domain.exception.LocationNotFoundException;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.port.in.FindWindowsQuery;
import com.mauricio.janela.domain.port.in.FindWindowsUseCase;
import com.mauricio.janela.domain.port.in.WindowsResult;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.domain.port.out.NarrativeGenerator;
import com.mauricio.janela.domain.port.out.WeatherProvider;
import com.mauricio.janela.domain.service.WindowScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Geocodes the city, scores the forecast and asks the model to explain the result. If the model fails,
 * the template writes the narrative instead: Java computes, the model only explains.
 */
public class FindWindowsService implements FindWindowsUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindWindowsService.class);

    private final GeocodingProvider geocodingProvider;
    private final WeatherProvider weatherProvider;
    private final WindowScorer windowScorer;
    private final NarrativeGenerator aiNarrativeGenerator;
    private final NarrativeGenerator fallbackNarrativeGenerator;
    private final Clock clock;

    public FindWindowsService(
            GeocodingProvider geocodingProvider,
            WeatherProvider weatherProvider,
            WindowScorer windowScorer,
            NarrativeGenerator aiNarrativeGenerator,
            NarrativeGenerator fallbackNarrativeGenerator,
            Clock clock) {
        this.geocodingProvider = geocodingProvider;
        this.weatherProvider = weatherProvider;
        this.windowScorer = windowScorer;
        this.aiNarrativeGenerator = aiNarrativeGenerator;
        this.fallbackNarrativeGenerator = fallbackNarrativeGenerator;
        this.clock = clock;
    }

    @Override
    public WindowsResult findWindows(FindWindowsQuery query) {
        Location location = geocodingProvider.findByName(query.city())
                .orElseThrow(() -> new LocationNotFoundException(query.city()));

        LocalDateTime now = LocalDateTime.now(clock.withZone(zoneOf(location)));
        List<HourlyForecast> upcoming = weatherProvider.getHourlyForecast(location, query.days()).stream()
                .filter(hour -> hour.time() != null && !hour.time().isBefore(now))
                .toList();

        List<OutdoorWindow> windows = windowScorer.findBestWindows(upcoming, query.activity(), query.durationMinutes());
        NarrativeRequest narrativeRequest = new NarrativeRequest(
                location, query.activity(), query.durationMinutes(), windows, query.language());

        return new WindowsResult(location, query.activity(), windows, narrate(narrativeRequest));
    }

    private Narrative narrate(NarrativeRequest request) {
        try {
            return aiNarrativeGenerator.generate(request);
        } catch (RuntimeException e) {
            log.warn("AI narrative unavailable, using template: {}", e.toString());
            return fallbackNarrativeGenerator.generate(request);
        }
    }

    /**
     * Forecast times are in the location's local time; without a valid timezone, fall back to the clock's.
     */
    private ZoneId zoneOf(Location location) {
        if (location.timezone() == null) {
            return clock.getZone();
        }
        try {
            return ZoneId.of(location.timezone());
        } catch (DateTimeException e) {
            return clock.getZone();
        }
    }
}
