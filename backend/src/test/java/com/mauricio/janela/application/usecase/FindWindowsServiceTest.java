package com.mauricio.janela.application.usecase;

import com.mauricio.janela.domain.exception.LocationNotFoundException;
import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.port.in.FindWindowsQuery;
import com.mauricio.janela.domain.port.in.WindowsResult;
import com.mauricio.janela.domain.port.out.GeocodingProvider;
import com.mauricio.janela.domain.port.out.NarrativeGenerator;
import com.mauricio.janela.domain.port.out.WeatherProvider;
import com.mauricio.janela.domain.service.WindowScorer;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindWindowsServiceTest {

    private static final Location CAMPINAS = new Location("Campinas", -22.9, -47.06, "America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);
    // 10:30 in São Paulo (UTC-3); the test JVM's default zone must not matter.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-06T13:30:00Z"), ZoneOffset.UTC);
    private static final FindWindowsQuery QUERY = new FindWindowsQuery("Campinas", Activity.WALK, 60, 1, Language.PT);

    private final GeocodingProvider geocoding = city -> Optional.of(CAMPINAS);
    private final WeatherProvider weather = (location, days) -> pleasantDay();
    private final NarrativeGenerator template = request -> Narrative.fromTemplate("template");

    @Test
    void ignoresHoursThatAlreadyStartedInTheLocationTimezone() {
        WindowsResult result = service(request -> Narrative.fromModel("ai", "gemma3:4b")).findWindows(QUERY);

        assertThat(result.windows()).isNotEmpty()
                .allSatisfy(window -> assertThat(window.start()).isAfterOrEqualTo(TODAY.atTime(11, 0)));
    }

    @Test
    void usesAiNarrativeAndPassesRankedWindowsToIt() {
        AtomicReference<NarrativeRequest> seen = new AtomicReference<>();
        NarrativeGenerator ai = request -> {
            seen.set(request);
            return Narrative.fromModel("ai", "gemma3:4b");
        };

        WindowsResult result = service(ai).findWindows(QUERY);

        assertThat(result.location()).isEqualTo(CAMPINAS);
        assertThat(result.activity()).isEqualTo(Activity.WALK);
        assertThat(result.narrative()).isEqualTo(Narrative.fromModel("ai", "gemma3:4b"));
        assertThat(seen.get().windows()).isEqualTo(result.windows());
        assertThat(seen.get().language()).isEqualTo(Language.PT);
    }

    @Test
    void fallsBackToTemplateWhenAiFails() {
        NarrativeGenerator brokenAi = request -> {
            throw new IllegalStateException("connection refused");
        };

        WindowsResult result = service(brokenAi).findWindows(QUERY);

        assertThat(result.narrative()).isEqualTo(Narrative.fromTemplate("template"));
        assertThat(result.windows()).isNotEmpty();
    }

    @Test
    void throwsWhenCityIsUnknown() {
        FindWindowsService service = new FindWindowsService(city -> Optional.empty(), weather, new WindowScorer(),
                template, template, CLOCK);

        assertThatThrownBy(() -> service.findWindows(QUERY)).isInstanceOf(LocationNotFoundException.class);
    }

    private FindWindowsService service(NarrativeGenerator ai) {
        return new FindWindowsService(geocoding, weather, new WindowScorer(), ai, template, CLOCK);
    }

    /** 06:00–18:00 daylight, mild and dry, so every daytime hour scores 100. */
    private static List<HourlyForecast> pleasantDay() {
        List<HourlyForecast> hours = new ArrayList<>();
        IntStream.range(0, 24).forEach(hour -> hours.add(new HourlyForecast(
                TODAY.atTime(hour, 0), 20.0, 20.0, 0, 1.0, 5.0, hour >= 6 && hour < 18)));
        return hours;
    }
}
