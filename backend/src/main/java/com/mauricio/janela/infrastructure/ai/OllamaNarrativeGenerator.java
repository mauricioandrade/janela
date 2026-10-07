package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Comfort;
import com.mauricio.janela.domain.model.DayOutlook;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.port.out.NarrativeGenerator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Asks the local Gemma model (via Ollama) to explain the windows. The model only receives the
 * ranked windows as JSON; it never computes or picks them. Throws on failure so the caller can fall back.
 */
@Component
public class OllamaNarrativeGenerator implements NarrativeGenerator {

    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;
    private final Resource systemPrompt;
    private final String model;

    public OllamaNarrativeGenerator(
            ChatClient.Builder chatClientBuilder,
            JsonMapper jsonMapper,
            @Value("classpath:prompts/narrative-system.st") Resource systemPrompt,
            @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.jsonMapper = jsonMapper;
        this.systemPrompt = systemPrompt;
        this.model = model;
    }

    @Override
    public Narrative generate(NarrativeRequest request) {
        String text = chatClient.prompt()
                .system(system -> system.text(systemPrompt).param("language", request.language().displayName()))
                .user(jsonMapper.writeValueAsString(PromptPayload.from(request)))
                .call()
                .content();

        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Ollama returned an empty narrative");
        }
        return Narrative.fromModel(text.strip(), model);
    }

    /**
     * The JSON the model sees. Kept flat and explicit so the prompt does not depend on domain refactors. Field names
     * are plain words because a small model sometimes echoes them.
     */
    record PromptPayload(
            String language,
            String city,
            String activity,
            int durationMinutes,
            String temperatureUnit,
            String windUnit,
            List<WindowPayload> windows,
            String challengeIdea
    ) {

        static PromptPayload from(NarrativeRequest request) {
            Language language = request.language();
            List<OutdoorWindow> windows = request.windows();
            return new PromptPayload(
                    language.code(),
                    request.location().name(),
                    request.activity().name(),
                    request.durationMinutes(),
                    language.temperatureSymbol(),
                    language.windSymbol(),
                    IntStream.range(0, windows.size())
                            .mapToObj(i -> WindowPayload.from(i + 1, windows.get(i), request, language))
                            .toList(),
                    windows.isEmpty() ? null
                            : ChallengeIdeas.pick(request.activity(), windows.getFirst(), request.location(), language));
        }
    }

    /**
     * Pre-digested so a small model rewrites conclusions instead of drawing them: day and times are formatted in the
     * target language, numbers are rounded and converted to the reader's units, temperature and UV come with words,
     * and {@code reasons} are the window's advantages already worked out from the day's outlook. The score is left
     * out on purpose; rank already says which window is best.
     */
    record WindowPayload(
            int rank,
            String day,
            String start,
            String end,
            Long temperature,
            String comfort,
            Long uvIndex,
            String uvLevel,
            Integer rainChancePercent,
            Long wind,
            List<String> reasons
    ) {

        static WindowPayload from(int rank, OutdoorWindow window, NarrativeRequest request, Language language) {
            Long uv = round(window.maxUv());
            Double feelsLike = window.apparentTempC();
            return new WindowPayload(rank, dayName(window.start(), language), window.start().format(TIME),
                    window.end().format(TIME),
                    round(feelsLike == null ? null : language.temperatureFromCelsius(feelsLike)),
                    feelsLike == null ? null : comfortWord(request.activity().comfortOf(feelsLike), language),
                    uv, uv == null ? null : uvLevel(uv, language), window.maxRainProbability(),
                    round(window.maxWindKmh() == null ? null : language.windFromKmh(window.maxWindKmh())),
                    request.outlookFor(window).map(outlook -> reasons(window, outlook, language)).orElse(List.of()));
        }

        /** WHO UV index categories. */
        static String uvLevel(long uv, Language language) {
            int category = uv <= 2 ? 0 : uv <= 5 ? 1 : uv <= 7 ? 2 : uv <= 10 ? 3 : 4;
            return (language == Language.PT
                    ? List.of("baixo", "moderado", "alto", "muito alto", "extremo")
                    : List.of("low", "moderate", "high", "very high", "extreme")).get(category);
        }

        static String comfortWord(Comfort comfort, Language language) {
            return switch (comfort) {
                case COOL -> language == Language.PT ? "fresco" : "cool";
                case PLEASANT -> language == Language.PT ? "agradável" : "pleasant";
                case WARM -> language == Language.PT ? "quente" : "warm";
                case HOT -> language == Language.PT ? "muito quente" : "hot";
            };
        }

        private static final int RAIN_WORTH_MENTIONING = 30;
        private static final double UV_WORTH_MENTIONING = 6;

        /**
         * Where the window sits against the day's heat, rain and strongest sun, as short phrases. Only peaks outside
         * the window count: a window that contains the peak gets no reason about it.
         */
        static List<String> reasons(OutdoorWindow window, DayOutlook outlook, Language language) {
            List<String> reasons = new ArrayList<>();
            boolean pt = language == Language.PT;
            if (outlook.hottestAt() != null) {
                String at = outlook.hottestAt().format(TIME);
                if (!outlook.hottestAt().isBefore(window.end())) {
                    reasons.add(pt ? "antes do pico de calor do dia, às " + at : "before the day's heat peaks at " + at);
                } else if (outlook.hottestAt().isBefore(window.start())) {
                    reasons.add(pt ? "depois do pico de calor do dia, às " + at : "after the day's heat peaked at " + at);
                }
            }
            if (outlook.maxRainProbability() != null && outlook.maxRainProbability() >= RAIN_WORTH_MENTIONING) {
                String at = outlook.wettestAt().format(TIME);
                if (!outlook.wettestAt().isBefore(window.end())) {
                    reasons.add(pt ? "antes da hora mais provável de chuva, às " + at
                            : "before the likeliest rain, at " + at);
                } else if (outlook.wettestAt().isBefore(window.start())) {
                    reasons.add(pt ? "depois da hora mais provável de chuva, às " + at
                            : "after the likeliest rain, at " + at);
                }
            }
            if (outlook.maxUv() != null && Math.round(outlook.maxUv()) >= UV_WORTH_MENTIONING) {
                String at = outlook.strongestSunAt().format(TIME);
                if (!outlook.strongestSunAt().isBefore(window.end())) {
                    reasons.add(pt ? "antes do sol mais forte, às " + at : "before the strongest sun, at " + at);
                } else if (outlook.strongestSunAt().isBefore(window.start())) {
                    reasons.add(pt ? "depois do sol mais forte, às " + at : "after the strongest sun, at " + at);
                }
            }
            return List.copyOf(reasons);
        }
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private static String dayName(LocalDateTime time, Language language) {
        return time.format(DateTimeFormatter.ofPattern(
                language == Language.PT ? "EEEE, dd/MM" : "EEEE, MMM d", language.locale()));
    }

    private static Long round(Double value) {
        return value == null ? null : Math.round(value);
    }
}
