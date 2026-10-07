package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Comfort;
import com.mauricio.janela.domain.model.DayOutlook;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.port.out.NarrativeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Asks the local Gemma model (via Ollama) to explain the windows. The model only receives the
 * ranked windows as JSON; it never computes or picks them. Throws on failure so the caller can fall back.
 */
@Component
public class OllamaNarrativeGenerator implements NarrativeGenerator {

    private static final Logger log = LoggerFactory.getLogger(OllamaNarrativeGenerator.class);

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
        PromptPayload payload = PromptPayload.from(request);
        String json = jsonMapper.writeValueAsString(payload);
        String text = ask(request, json);
        Optional<String> problem = NarrativeCheck.problem(text, payload);
        if (problem.isPresent()) {
            // One second try, told what was wrong; a small model usually fixes a named mistake.
            log.info("Gemma narrative rejected ({}), asking again: {}", problem.get(), text.strip());
            text = ask(request, json + "\n\nYour previous answer broke a rule (" + problem.get()
                    + "). Write it again following every rule.");
            problem = NarrativeCheck.problem(text, payload);
        }
        if (problem.isPresent()) {
            throw new IllegalStateException("Gemma narrative rejected twice (" + problem.get() + "): " + text.strip());
        }
        return Narrative.fromModel(NarrativeCheck.tidy(text), model);
    }

    private String ask(NarrativeRequest request, String user) {
        String text = chatClient.prompt()
                .system(system -> system.text(systemPrompt).param("language", request.language().displayName()))
                .user(user)
                .call()
                .content();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Ollama returned an empty narrative");
        }
        return text;
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
            WindowPayload best,
            WindowPayload alternative,
            String challengeIdea
    ) {

        static PromptPayload from(NarrativeRequest request) {
            Language language = request.language();
            // The best window and one alternative, by name: given a ranked list of three, the model mixed up the order,
            // praised the wrong one and wrote "Rank 2" into the text.
            List<OutdoorWindow> windows = request.windows();
            return new PromptPayload(
                    language.code(),
                    request.location().name(),
                    request.activity().name(),
                    request.durationMinutes(),
                    language.temperatureSymbol(),
                    language.windSymbol(),
                    windows.isEmpty() ? null : WindowPayload.from(windows.getFirst(), request, language),
                    windows.size() < 2 ? null : WindowPayload.from(windows.get(1), request, language),
                    windows.isEmpty() ? null
                            : ChallengeIdeas.pick(request.activity(), windows.getFirst(),
                            request.outlookFor(windows.getFirst()).map(DayOutlook::lastLightAt).orElse(null),
                            request.location(), language));
        }
    }

    /**
     * Pre-digested so a small model rewrites conclusions instead of drawing them: day and times are formatted in the
     * target language, numbers are rounded and converted to the reader's units, the window's quality, temperature, UV
     * and rain come with words, and {@code reasons} are the window's advantages already worked out from the day's
     * outlook. The score itself is left out on purpose: the model kept quoting it; {@code rating} is the same word the
     * screen shows next to it.
     */
    record WindowPayload(
            String rating,
            String day,
            String start,
            String end,
            Long temperature,
            String comfort,
            Long uvIndex,
            String uvLevel,
            Integer rainChancePercent,
            String rainLevel,
            Long wind,
            List<String> reasons
    ) {

        static WindowPayload from(OutdoorWindow window, NarrativeRequest request, Language language) {
            Long uv = round(window.maxUv());
            Double feelsLike = window.apparentTempC();
            Integer rain = window.maxRainProbability();
            return new WindowPayload(rating(window.score(), language), dayName(window.start(), language),
                    window.start().format(TIME),
                    window.end().format(TIME),
                    round(feelsLike == null ? null : language.temperatureFromCelsius(feelsLike)),
                    feelsLike == null ? null : comfortWord(request.activity().comfortOf(feelsLike), language),
                    uv, uv == null ? null : uvLevel(uv, language), rain, rain == null ? null : rainLevel(rain, language),
                    round(window.maxWindKmh() == null ? null : language.windFromKmh(window.maxWindKmh())),
                    request.outlookFor(window).map(outlook -> reasons(window, outlook, language)).orElse(List.of()));
        }

        /** The score's word, with the screen's thresholds (lib/score.ts on the frontend). */
        static String rating(int score, Language language) {
            int tier = score >= 70 ? 0 : score >= 40 ? 1 : 2;
            return (language == Language.PT ? List.of("ótima", "razoável", "fraca") : List.of("great", "fair", "poor"))
                    .get(tier);
        }

        /** Rain chance as a word; from 40% the screen already warns of likely rain. */
        static String rainLevel(int chance, Language language) {
            int level = chance < 20 ? 0 : chance < 40 ? 1 : 2;
            return (language == Language.PT ? List.of("baixa", "moderada", "alta") : List.of("low", "moderate", "high"))
                    .get(level);
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
