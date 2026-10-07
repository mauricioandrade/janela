package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.DayOutlook;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.model.TestLocations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OllamaNarrativeGeneratorTest {

    private static final NarrativeRequest REQUEST = new NarrativeRequest(
            TestLocations.CAMPINAS,
            Activity.RUN,
            60,
            List.of(new OutdoorWindow(LocalDateTime.of(2026, 10, 6, 6, 0), LocalDateTime.of(2026, 10, 6, 7, 0),
                    91, 19.4, 1.2, 5, 8.0)),
            Language.EN,
            List.of(
                    new DayOutlook(LocalDate.of(2026, 10, 6), LocalDateTime.of(2026, 10, 6, 13, 0), 33.6,
                            LocalDateTime.of(2026, 10, 6, 16, 0), 60, LocalDateTime.of(2026, 10, 6, 12, 0), 10.6),
                    // A day with no window: left out of the payload.
                    new DayOutlook(LocalDate.of(2026, 10, 7), null, null, null, null, null, null)));

    private final ChatModel chatModel = mock(ChatModel.class);
    private final OllamaNarrativeGenerator generator;

    OllamaNarrativeGeneratorTest() {
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        generator = new OllamaNarrativeGenerator(
                ChatClient.builder(chatModel),
                JsonMapper.builder().build(),
                new ClassPathResource("prompts/narrative-system.st"),
                "gemma3:4b");
    }

    @Test
    void sendsRankedWindowsAsJsonAndRendersLanguageInSystemPrompt() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("  Go at 06:00.\n🌿 Notice three bird calls.  "));

        Narrative narrative = generator.generate(REQUEST);

        assertThat(narrative).isEqualTo(Narrative.fromModel("Go at 06:00.\n\n🌿 Notice three bird calls.", "gemma3:4b"));

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getSystemMessage().getText())
                .contains("Write in English.")
                .doesNotContain("{language}");
        assertThat(prompt.getValue().getUserMessage().getText())
                .contains("\"city\":\"Campinas\"", "\"activity\":\"RUN\"", "\"best\":{",
                        "\"day\":\"Tuesday, Oct 6\"", "\"start\":\"06:00\"", "\"end\":\"07:00\"",
                        "\"temperatureUnit\":\"°F\"", "\"temperature\":67", "\"comfort\":\"pleasant\"",
                        "\"uvIndex\":1", "\"uvLevel\":\"low\"", "\"rainChancePercent\":5",
                        "\"rainLevel\":\"low\"", "\"rating\":\"great\"",
                        "\"windUnit\":\"mph\"", "\"wind\":5",
                        "\"reasons\":[\"before the day's heat peaks at 13:00\",\"before the likeliest rain, at 16:00\","
                                + "\"before the strongest sun, at 12:00\"]", "\"challengeIdea\":\"")
                .doesNotContain("score", "Wednesday", "hottest");
    }

    @Test
    void sendsOnlyTheBestWindowAndOneAlternative() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("Go at 16:00.\n🌿 Count three birds."));
        LocalDateTime four = LocalDateTime.of(2026, 10, 6, 16, 0);
        NarrativeRequest threeWindows = new NarrativeRequest(TestLocations.CAMPINAS, Activity.WORKOUT, 60,
                List.of(new OutdoorWindow(four, four.plusHours(1), 53, 27.6, 2.2, 47, 5.8),
                        new OutdoorWindow(four.plusHours(1), four.plusHours(2), 51, 27.1, 2.0, 54, 9.6),
                        new OutdoorWindow(four.plusHours(2), four.plusHours(3), 50, 26.9, 0.2, 57, 3.8)),
                Language.PT, List.of());

        generator.generate(threeWindows);

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText())
                .contains("\"best\":{", "\"alternative\":{", "\"rating\":\"razoável\"",
                        "\"rainLevel\":\"alta\"", "\"comfort\":\"muito quente\"")
                .doesNotContain("rank", "\"start\":\"18:00\"");
    }

    @ParameterizedTest
    @CsvSource({"69, fair", "70, great", "40, fair", "39, poor"})
    void ratesWithTheScreensThresholds(int score, String rating) {
        assertThat(OllamaNarrativeGenerator.WindowPayload.rating(score, Language.EN)).isEqualTo(rating);
    }

    @ParameterizedTest
    @CsvSource({"0, low", "19, low", "20, moderate", "39, moderate", "40, high", "100, high"})
    void wordsRainChanceLikeTheScreenWarns(int chance, String level) {
        assertThat(OllamaNarrativeGenerator.WindowPayload.rainLevel(chance, Language.EN)).isEqualTo(level);
    }

    @ParameterizedTest
    @CsvSource({"0, low", "2, low", "3, moderate", "5, moderate", "6, high", "7, high", "8, very high",
            "10, very high", "11, extreme"})
    void classifiesUvWithWhoCategories(long uv, String level) {
        assertThat(OllamaNarrativeGenerator.WindowPayload.uvLevel(uv, Language.EN)).isEqualTo(level);
    }

    @Test
    void reasonsOnlyCountPeaksOutsideTheWindow() {
        OutdoorWindow afternoon = new OutdoorWindow(LocalDateTime.of(2026, 10, 6, 15, 0),
                LocalDateTime.of(2026, 10, 6, 17, 0), 70, 30.0, 4.0, 40, 10.0);
        DayOutlook outlook = new DayOutlook(LocalDate.of(2026, 10, 6), LocalDateTime.of(2026, 10, 6, 13, 0), 34.0,
                LocalDateTime.of(2026, 10, 6, 16, 0), 70, LocalDateTime.of(2026, 10, 6, 12, 0), 11.0);

        assertThat(OllamaNarrativeGenerator.WindowPayload.reasons(afternoon, outlook, Language.PT))
                .containsExactly("depois do pico de calor do dia, às 13:00", "depois do sol mais forte, às 12:00");
    }

    @Test
    void asksAgainOnceWithTheProblemNamed() {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(reply("Go at 6:00 AM.\n🌿 Count three birds."), reply("Tuesday, Oct 6 at 06:00 is pleasant.\n🌿 Count three birds."));

        Narrative narrative = generator.generate(REQUEST);

        assertThat(narrative.text()).isEqualTo("Tuesday, Oct 6 at 06:00 is pleasant.\n\n🌿 Count three birds.");
        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(2)).call(prompts.capture());
        assertThat(prompts.getAllValues().get(1).getUserMessage().getText())
                .contains("Your previous answer broke a rule (12-hour time 6:00 am)");
    }

    @Test
    void givesUpAfterTheSecondBrokenTextSoTheCallerCanFallBack() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("Go at 6:00 AM by the beach."));

        assertThatThrownBy(() -> generator.generate(REQUEST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected twice");
        verify(chatModel, times(2)).call(any(Prompt.class));
    }

    @Test
    void failsOnBlankReplySoTheCallerCanFallBack() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("   "));

        assertThatThrownBy(() -> generator.generate(REQUEST)).isInstanceOf(IllegalStateException.class);
    }

    private static ChatResponse reply(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
