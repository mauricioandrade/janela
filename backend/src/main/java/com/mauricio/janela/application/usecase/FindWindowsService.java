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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Location location = (query.cityId() != null
                ? geocodingProvider.findById(query.cityId(), query.language())
                : geocodingProvider.findByName(query.city(), query.language()))
                .orElseThrow(() -> new LocationNotFoundException(query.city()));

        List<HourlyForecast> upcoming = upcomingDays(location, query.days());

        List<OutdoorWindow> windows = windowScorer.findBestWindows(upcoming, query.activity(), query.durationMinutes());
        NarrativeRequest narrativeRequest = new NarrativeRequest(
                location, query.activity(), query.durationMinutes(), windows, query.language());

        return new WindowsResult(location, query.activity(), windows, narrate(narrativeRequest));
    }

    /**
     * Hours from now on, over the next {@code days} days that still have daylight ahead. After sunset today
     * counts no more, so one extra day is fetched to keep "3 days" meaning three days you can still go out.
     */
    private List<HourlyForecast> upcomingDays(Location location, int days) {
        LocalDateTime now = LocalDateTime.now(clock.withZone(zoneOf(location)));
        List<HourlyForecast> upcoming = weatherProvider.getHourlyForecast(location, days + 1).stream()
                .filter(hour -> hour.time() != null && !hour.time().isBefore(now))
                .toList();
        Set<LocalDate> daylightDays = upcoming.stream()
                .filter(hour -> !Boolean.FALSE.equals(hour.isDay()))
                .map(hour -> hour.time().toLocalDate())
                .distinct()
                .sorted()
                .limit(days)
                .collect(Collectors.toSet());
        return upcoming.stream().filter(hour -> daylightDays.contains(hour.time().toLocalDate())).toList();
    }

    /**
     * With no windows there is nothing to explain, and a small model asked to say so tends to invent a time
     * anyway; the template answers instead.
     */
    private Narrative narrate(NarrativeRequest request) {
        if (request.windows().isEmpty()) {
            return fallbackNarrativeGenerator.generate(request);
        }
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
