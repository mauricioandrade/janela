package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.port.out.NarrativeGenerator;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Offline narrative assembled in Java. Used when the local model is unavailable, so the app works with zero AI.
 */
@Component
public class TemplateNarrativeGenerator implements NarrativeGenerator {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Narrative generate(NarrativeRequest request) {
        String text = switch (request.language()) {
            case PT -> portuguese(request);
            case EN -> english(request);
        };
        return Narrative.fromTemplate(text);
    }

    private static String portuguese(NarrativeRequest request) {
        String activity = activityName(request.activity(), Language.PT);
        String city = request.location().name();
        if (request.windows().isEmpty()) {
            return "Não encontrei uma boa janela para " + activity + " em " + city + " nesse período. "
                    + "Que tal cinco minutos na varanda ou perto da janela, só olhando o céu?\n"
                    + "🌿 Abra a janela e conte três sons diferentes lá fora.";
        }

        OutdoorWindow best = request.windows().getFirst();
        DateTimeFormatter day = DateTimeFormatter.ofPattern("EEEE, dd/MM", PT_BR);
        StringBuilder text = new StringBuilder()
                .append("A melhor janela para ").append(activity).append(" em ").append(city)
                .append(" é ").append(best.start().format(day))
                .append(", das ").append(best.start().format(TIME)).append(" às ").append(best.end().format(TIME))
                .append(" (nota ").append(best.score()).append("/100)");

        List<String> conditions = new ArrayList<>();
        if (best.apparentTempC() != null) {
            conditions.add("sensação de " + decimal(best.apparentTempC(), PT_BR) + " °C");
        }
        if (best.maxUv() != null) {
            conditions.add("UV até " + decimal(best.maxUv(), PT_BR));
        }
        if (best.maxRainProbability() != null) {
            conditions.add("chuva até " + best.maxRainProbability() + "%");
        }
        if (best.maxWindKmh() != null) {
            conditions.add("vento até " + decimal(best.maxWindKmh(), PT_BR) + " km/h");
        }
        appendConditions(text, conditions, " e ");

        appendAlternatives(text, request.windows(), " Outras opções: ", " e ", day);
        return text.append("\n🌿 ").append(challenge(request, best, Language.PT)).toString();
    }

    private static String english(NarrativeRequest request) {
        String activity = activityName(request.activity(), Language.EN);
        String city = request.location().name();
        if (request.windows().isEmpty()) {
            return "I couldn't find a good window for a " + activity + " in " + city + " in this period. "
                    + "How about five minutes on a balcony or by the window, just watching the sky?\n"
                    + "🌿 Open the window and count three different sounds outside.";
        }

        OutdoorWindow best = request.windows().getFirst();
        DateTimeFormatter day = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH);
        StringBuilder text = new StringBuilder()
                .append("The best window for a ").append(activity).append(" in ").append(city)
                .append(" is ").append(best.start().format(day))
                .append(", ").append(best.start().format(TIME)).append("–").append(best.end().format(TIME))
                .append(" (score ").append(best.score()).append("/100)");

        List<String> conditions = new ArrayList<>();
        if (best.apparentTempC() != null) {
            conditions.add("feels like " + decimal(Language.EN.temperatureFromCelsius(best.apparentTempC()),
                    Locale.ENGLISH) + " " + Language.EN.temperatureSymbol());
        }
        if (best.maxUv() != null) {
            conditions.add("UV up to " + decimal(best.maxUv(), Locale.ENGLISH));
        }
        if (best.maxRainProbability() != null) {
            conditions.add("rain chance up to " + best.maxRainProbability() + "%");
        }
        if (best.maxWindKmh() != null) {
            conditions.add("wind up to " + decimal(Language.EN.windFromKmh(best.maxWindKmh()), Locale.ENGLISH) + " "
                    + Language.EN.windSymbol());
        }
        appendConditions(text, conditions, " and ");

        appendAlternatives(text, request.windows(), " Other options: ", " and ", day);
        return text.append("\n🌿 ").append(challenge(request, best, Language.EN)).toString();
    }

    private static void appendConditions(StringBuilder text, List<String> conditions, String lastSeparator) {
        if (!conditions.isEmpty()) {
            text.append(": ").append(joinNaturally(conditions, lastSeparator));
        }
        text.append('.');
    }

    private static void appendAlternatives(StringBuilder text, List<OutdoorWindow> windows, String prefix,
                                           String lastSeparator, DateTimeFormatter day) {
        if (windows.size() < 2) {
            return;
        }
        List<String> alternatives = windows.subList(1, windows.size()).stream()
                .map(window -> window.start().format(day) + " " + window.start().format(TIME)
                        + "–" + window.end().format(TIME))
                .toList();
        text.append(prefix).append(joinNaturally(alternatives, lastSeparator)).append('.');
    }

    private static String joinNaturally(List<String> items, String lastSeparator) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + lastSeparator + items.getLast();
    }

    private static String decimal(double value, Locale locale) {
        return String.format(locale, "%.1f", value);
    }

    private static String activityName(Activity activity, Language language) {
        return switch (language) {
            case PT -> switch (activity) {
                case RUN -> "correr";
                case WALK -> "caminhar";
                case BIKE -> "pedalar";
                case PICNIC -> "um piquenique";
                case GARDENING -> "jardinagem";
            };
            case EN -> switch (activity) {
                case RUN -> "run";
                case WALK -> "walk";
                case BIKE -> "bike ride";
                case PICNIC -> "picnic";
                case GARDENING -> "gardening session";
            };
        };
    }

    /** The same idea bank the model gets, written out as a sentence. */
    private static String challenge(NarrativeRequest request, OutdoorWindow best, Language language) {
        String idea = ChallengeIdeas.pick(request.activity(), best, request.location(), language);
        return Character.toUpperCase(idea.charAt(0)) + idea.substring(1) + ".";
    }
}
