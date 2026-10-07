package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.infrastructure.ai.OllamaNarrativeGenerator.PromptPayload;
import com.mauricio.janela.infrastructure.ai.OllamaNarrativeGenerator.WindowPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NarrativeCheckTest {

    // Itobi, outdoor workout: a fair, hot best window and a rainier alternative.
    private static final PromptPayload ITOBI = payload("Itobi",
            window("razoável", "16:00", "17:00", "muito quente", List.of("antes da hora mais provável de chuva, às 18:00")),
            window("razoável", "17:00", "18:00", "muito quente", List.of()));

    @Test
    void acceptsAnHonestText() {
        String text = "Quarta-feira, 07/10, às 16:00 é a melhor opção do dia em Itobi, antes da chuva das 18:00, "
                + "mas vai estar muito quente. Às 17:00 a chance de chuva é maior.\n🌿 Faça a última série descalço.";

        assertThat(NarrativeCheck.problem(text, ITOBI)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Às 16:00 é o momento ideal para o seu treino.",                 // praise for a fair window
            "Às 16:00 o ar está fresco.",                                    // mild words for muito quente
            "Às 19:00 ainda dá tempo.",                                      // a time it was not given
            "Go at 4:00 PM.",                                                // 12-hour clock
            "Rank 2 is at 17:00.",                                           // field name
            "Às 16:00, treine perto da praia.",                              // invented place
            "Às 16:00 é melhor você ir agora.",                              // relative day
            "Às 16:00 é a melhor opção.",                                    // no challenge
            "Às 16:00 é a melhor opção.\n\n🌿"                               // empty challenge
    })
    void rejectsWhatTheModelGotWrongInTesting(String text) {
        assertThat(NarrativeCheck.problem(text, ITOBI)).isPresent();
    }

    @Test
    void allowsMildWordsAndPraiseWhenTheDataSaysSo() {
        PromptPayload lisbon = payload("Lisbon", window("great", "10:00", "11:00", "pleasant", List.of()), null);

        assertThat(NarrativeCheck.problem("Thursday at 10:00 is ideal: pleasant and calm in Lisbon.\n🌿 Count three clouds.", lisbon)).isEmpty();
    }

    @Test
    void rejectsPraiseForHeatEvenInAGreatWindow() {
        PromptPayload recife = payload("Recife", window("ótima", "06:00", "07:00", "muito quente", List.of()), null);

        assertThat(NarrativeCheck.problem("Às 06:00 vai estar muito quente, ideal para correr.\n🌿 Conte três pássaros.", recife)).isPresent();
    }

    @Test
    void allowsComparingTheMorningToTheHeat() {
        PromptPayload manaus = payload("Manaus", window("great", "06:00", "07:00", "warm", List.of()), null);

        assertThat(NarrativeCheck.problem("Thursday at 06:00 is ideal: the coolest part of the morning.\n🌿 Count three birds.", manaus))
                .isEmpty();
        assertThat(NarrativeCheck.problem("Às 06:00 a hora é mais fresca.\n🌿 Conte três pássaros.", manaus)).isEmpty();
        assertThat(NarrativeCheck.problem("Às 06:00 a hora é fresca.\n🌿 Conte três pássaros.", manaus)).isPresent();
    }

    @Test
    void rejectsDataInTheChallengeLine() {
        assertThat(NarrativeCheck.problem("Às 16:00 é a melhor opção.\n🌿 Sinta o vento de 8 km/h.", ITOBI))
                .isPresent();
    }

    @Test
    void ignoresFlaggedWordsInsideTheCityName() {
        PromptPayload rio = payload("Rio de Janeiro", window("great", "06:00", "07:00", "agradável", List.of()), null);

        assertThat(NarrativeCheck.problem("Às 06:00 o Rio de Janeiro está agradável.\n🌿 Conte três pássaros.", rio)).isEmpty();
    }

    @Test
    void putsTheChallengeOnItsOwnParagraph() {
        assertThat(NarrativeCheck.tidy("Go at 06:00. 🌿  Count three birds. "))
                .isEqualTo("Go at 06:00.\n\n🌿 Count three birds.");
        assertThat(NarrativeCheck.tidy("Go at 06:00.\n\n\n🌿 Count three birds."))
                .isEqualTo("Go at 06:00.\n\n🌿 Count three birds.");
    }

    private static PromptPayload payload(String city, WindowPayload best, WindowPayload alternative) {
        return new PromptPayload("pt", city, "WORKOUT", 60, "°C", "km/h", best, alternative, "idea");
    }

    private static WindowPayload window(String rating, String start, String end, String comfort, List<String> reasons) {
        return new WindowPayload(rating, "quarta-feira, 07/10", start, end, 28L, comfort, 2L, "baixo", 47, "alta", 6L,
                reasons);
    }
}
