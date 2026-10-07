package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.model.TestLocations;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateNarrativeGeneratorTest {

    private static final Location CAMPINAS = TestLocations.CAMPINAS;
    private static final OutdoorWindow MORNING = new OutdoorWindow(
            LocalDateTime.of(2026, 10, 6, 6, 0), LocalDateTime.of(2026, 10, 6, 7, 0), 91, 19.4, 1.2, 5, 8.0);
    private static final OutdoorWindow EVENING = new OutdoorWindow(
            LocalDateTime.of(2026, 10, 6, 17, 0), LocalDateTime.of(2026, 10, 6, 18, 0), 74, null, null, null, null);

    private final TemplateNarrativeGenerator generator = new TemplateNarrativeGenerator();

    @Test
    void describesBestWindowInPortuguese() {
        Narrative narrative = generator.generate(request(List.of(MORNING, EVENING), Language.PT));

        assertThat(narrative.aiGenerated()).isFalse();
        assertThat(narrative.model()).isNull();
        assertThat(narrative.text())
                .startsWith("A melhor janela para correr em Campinas é terça-feira, 06/10, das 06:00 às 07:00 (nota 91/100)")
                .contains("sensação de 19,4 °C", "UV até 1,2", "chuva até 5%", "vento até 8,0 km/h")
                .contains("Outras opções: terça-feira, 06/10 17:00–18:00.")
                .contains("\n🌿 ");
    }

    @Test
    void givesEnglishReadersFahrenheitAndMph() {
        Narrative narrative = generator.generate(request(List.of(MORNING), Language.EN));

        assertThat(narrative.text()).contains("feels like 66.9 °F", "wind up to 5.0 mph").doesNotContain("°C", "km/h");
    }

    @Test
    void describesBestWindowInEnglishAndSkipsMissingValues() {
        Narrative narrative = generator.generate(request(List.of(EVENING), Language.EN));

        assertThat(narrative.text())
                .startsWith("The best window for a run in Campinas is Tuesday, Oct 6, 17:00–18:00 (score 74/100).")
                .doesNotContain("Other options", "null")
                .contains("\n🌿 ");
    }

    @Test
    void suggestsIndoorToOutdoorAlternativeWhenThereAreNoWindows() {
        Narrative narrative = generator.generate(request(List.of(), Language.EN));

        assertThat(narrative.text()).contains("couldn't find a good window", "balcony", "\n🌿 ");
    }

    private static NarrativeRequest request(List<OutdoorWindow> windows, Language language) {
        return new NarrativeRequest(CAMPINAS, Activity.RUN, 60, windows, language);
    }
}
