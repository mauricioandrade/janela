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
            String city,
            String activity,
            int durationMinutes,
            List<WindowPayload> windows
    ) {

        static PromptPayload from(NarrativeRequest request) {
            return new PromptPayload(
                    request.language().code(),
                    request.location().name(),
                    request.activity().name(),
                    request.durationMinutes(),
                    IntStream.range(0, request.windows().size())
                            .mapToObj(i -> WindowPayload.from(i + 1, request.windows().get(i), request.language()))
                            .toList());
        }
    }

    /**
     * Day and times are pre-formatted in the target language so the model copies them instead of parsing ISO dates.
     */
    record WindowPayload(
            int rank,
            String day,
            String start,
            String end,
            int score,
            Double apparentTempC,
            Double maxUv,
            Integer maxRainProbability,
            Double maxWindKmh
    ) {

        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

        static WindowPayload from(int rank, OutdoorWindow window, Language language) {
            DateTimeFormatter day = DateTimeFormatter.ofPattern(
                    language == Language.PT ? "EEEE, dd/MM" : "EEEE, MMM d", language.locale());
            return new WindowPayload(rank, window.start().format(day), window.start().format(TIME),
                    window.end().format(TIME), window.score(), window.apparentTempC(),
                    window.maxUv(), window.maxRainProbability(), window.maxWindKmh());
        }
    }
}
