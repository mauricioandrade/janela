package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.infrastructure.ai.OllamaNarrativeGenerator.PromptPayload;
import com.mauricio.janela.infrastructure.ai.OllamaNarrativeGenerator.WindowPayload;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reads the model's text against the payload it was given and names the first rule it breaks, the ones a 4B model
 * still broke in testing despite the prompt. A rejected text is replaced by the template, so the screen never shows
 * a time, a place or a "pleasant" the data does not support.
 */
final class NarrativeCheck {

    private static final Pattern TIME = Pattern.compile("\\b(\\d{1,2})[:h](\\d{2})\\b");
    private static final Pattern TWELVE_HOUR = Pattern.compile("\\b\\d{1,2}(:\\d{2})?\\s?(am|pm|a\\.m\\.|p\\.m\\.)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FIELD_NAME = Pattern.compile(
            "\\b(rank|ranking|rating|uvLevel|rainLevel|challengeIdea|reasons)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVENTED_PLACE = Pattern.compile(
            "\\b(beach|beachfront|seaside|sea|ocean|waves?|park|river|lake"
                    + "|praia|orla|mar|oceano|ondas?|parque|rio|lago)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    // Absolute words only: "the coolest part of the morning" or "mais fresca" compares, and is true before the heat.
    private static final Pattern MILD_WORDS = Pattern.compile(
            "(?<!mais\\s)\\b(cool|fresh|pleasant|mild|fresco|fresca|frescor|fresquinho|agradável|agradáveis|ameno|amena)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    // The window happens on the day named; "go now" on a window two days out is wrong, and so is "agora" at 14:00.
    private static final Pattern RELATIVE_DAY = Pattern.compile(
            "\\b(now|today|tomorrow|tonight|agora|hoje|amanhã|esta noite)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern PRAISE = Pattern.compile("\\b(ideal|perfect|perfeito|perfeita)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private static final Set<String> MILD_COMFORT = Set.of("cool", "pleasant", "fresco", "agradável");
    private static final Set<String> GREAT_RATING = Set.of("great", "ótima");
    private static final Set<String> HOT_COMFORT = Set.of("hot", "muito quente");

    private NarrativeCheck() {
    }

    /** The first rule the text breaks, or empty when it can be shown. */
    static Optional<String> problem(String text, PromptPayload payload) {
        // The city may itself contain a flagged word (Rio de Janeiro, Mar del Plata).
        String body = text.replace(payload.city(), " ");
        Set<String> allowedTimes = allowedTimes(payload);
        Matcher time = TIME.matcher(body);
        while (time.find()) {
            String found = "%02d:%s".formatted(Integer.parseInt(time.group(1)), time.group(2));
            if (!allowedTimes.contains(found)) return Optional.of("time " + time.group() + " is not in the payload");
        }
        Optional<String> challenge = challenge(text);
        if (challenge.map(line -> line.replace("🌿", "").strip().split("\\s+").length < 3).orElse(true)) {
            return Optional.of("no 🌿 challenge line at the end");
        }
        return Stream.of(
                        firstMatch(TWELVE_HOUR, body).map(match -> "12-hour time " + match),
                        firstMatch(FIELD_NAME, body).map(match -> "field name " + match),
                        firstMatch(RELATIVE_DAY, body).map(match -> "relative day " + match),
                        firstMatch(INVENTED_PLACE, body).map(match -> "invented place " + match),
                        MILD_COMFORT.contains(payload.best().comfort()) ? Optional.<String>empty()
                                : firstMatch(MILD_WORDS, body).map(match -> "calls " + payload.best().comfort()
                                + " weather " + match),
                        // Praise needs a great window that is not also very hot: no "muito quente, ideal".
                        GREAT_RATING.contains(payload.best().rating())
                                && !HOT_COMFORT.contains(payload.best().comfort()) ? Optional.<String>empty()
                                : firstMatch(PRAISE, body).map(match -> "calls a " + payload.best().rating() + ", "
                                + payload.best().comfort() + " window " + match),
                        challenge.filter(line -> line.matches("(?s).*\\d.*"))
                                .map(line -> "numbers or times in the 🌿 line"))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** Everything from the 🌿 on: the challenge line, which should carry no data. */
    private static Optional<String> challenge(String text) {
        int start = text.indexOf("🌿");
        return start < 0 ? Optional.empty() : Optional.of(text.substring(start));
    }

    /** Puts the challenge on its own paragraph, however the model spaced it. */
    static String tidy(String text) {
        return text.strip().replaceAll("[ \\t]*\\R*[ \\t]*🌿[ \\t]*", "\n\n🌿 ").strip();
    }

    private static Set<String> allowedTimes(PromptPayload payload) {
        Set<String> times = new HashSet<>();
        for (WindowPayload window : Stream.of(payload.best(), payload.alternative()).filter(w -> w != null).toList()) {
            times.add(window.start());
            times.add(window.end());
            for (String reason : window.reasons()) {
                Matcher matcher = TIME.matcher(reason);
                while (matcher.find()) times.add(matcher.group());
            }
        }
        return times;
    }

    private static Optional<String> firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Optional.of(matcher.group().toLowerCase(Locale.ROOT)) : Optional.empty();
    }
}
