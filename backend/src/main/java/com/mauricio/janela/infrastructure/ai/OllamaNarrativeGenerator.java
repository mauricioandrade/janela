package com.mauricio.janela.infrastructure.ai;

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

import java.time.format.DateTimeFormatter;
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
     * The JSON the model sees. Kept flat and explicit so the prompt does not depend on domain refactors.
     */
    record PromptPayload(
            String language,
            String temperatureUnit,
            String city,
            String activity,
            int durationMinutes,
            List<WindowPayload> windows
    ) {

        static PromptPayload from(NarrativeRequest request) {
            return new PromptPayload(
                    request.language().code(),
                    request.language().temperatureSymbol(),
                    request.location().name(),
                    request.activity().name(),
                    request.durationMinutes(),
                    IntStream.range(0, request.windows().size())
                            .mapToObj(i -> WindowPayload.from(i + 1, request.windows().get(i), request.language()))
                            .toList());
        }
    }

    /**
     * Pre-digested so a small model copies instead of interpreting: day and times are formatted in the target
     * language, numbers are rounded the way the UI shows them, and UV comes with its WHO category. The score is
     * left out on purpose; rank already says which window is best and the model kept quoting the raw number.
     */
    record WindowPayload(
            int rank,
            String day,
            String start,
            String end,
            Long feelsLike,
            Long uvIndex,
            String uvLevel,
            Integer rainChancePercent,
            Long windKmh
    ) {

        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

        static WindowPayload from(int rank, OutdoorWindow window, Language language) {
            DateTimeFormatter day = DateTimeFormatter.ofPattern(
                    language == Language.PT ? "EEEE, dd/MM" : "EEEE, MMM d", language.locale());
            Long uv = round(window.maxUv());
            return new WindowPayload(rank, window.start().format(day), window.start().format(TIME),
                    window.end().format(TIME), round(window.apparentTempC() == null ? null
                    : language.temperatureFromCelsius(window.apparentTempC())), uv,
                    uv == null ? null : uvLevel(uv, language), window.maxRainProbability(),
                    round(window.maxWindKmh()));
        }

        private static Long round(Double value) {
            return value == null ? null : Math.round(value);
        }

        /** WHO UV index categories. */
        static String uvLevel(long uv, Language language) {
            int category = uv <= 2 ? 0 : uv <= 5 ? 1 : uv <= 7 ? 2 : uv <= 10 ? 3 : 4;
            return (language == Language.PT
                    ? List.of("baixo", "moderado", "alto", "muito alto", "extremo")
                    : List.of("low", "moderate", "high", "very high", "extreme")).get(category);
        }
    }
}
