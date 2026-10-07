package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.OutdoorWindow;
import com.mauricio.janela.domain.model.TestLocations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChallengeIdeasTest {

    @Test
    void picksTheSameIdeaForTheSameSearch() {
        OutdoorWindow window = window(LocalDateTime.of(2026, 10, 8, 6, 0));

        assertThat(ChallengeIdeas.pick(Activity.RUN, window, TestLocations.CAMPINAS, Language.PT))
                .isEqualTo(ChallengeIdeas.pick(Activity.RUN, window, TestLocations.CAMPINAS, Language.PT));
    }

    @Test
    void variesAcrossPlacesAndDays() {
        Set<String> ideas = new HashSet<>();
        for (String city : new String[]{"Recife", "Manaus", "Curitiba", "Salvador", "Natal"}) {
            for (int day = 7; day <= 9; day++) {
                Location place = new Location(null, city, null, null, "BR", 0, 0, "UTC");
                ideas.add(ChallengeIdeas.pick(Activity.WALK, window(LocalDateTime.of(2026, 10, day, 6, 0)), place,
                        Language.PT));
            }
        }
        assertThat(ideas).hasSizeGreaterThanOrEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(Activity.class)
    void everyActivityHasIdeasInBothLanguagesForEveryPartOfTheDay(Activity activity) {
        for (int hour : new int[]{6, 12, 17}) {
            assertThat(ChallengeIdeas.poolFor(activity, hour, Language.PT)).hasSizeGreaterThanOrEqualTo(6)
                    .allSatisfy(idea -> assertThat(idea).isNotBlank());
            assertThat(ChallengeIdeas.poolFor(activity, hour, Language.EN))
                    .hasSameSizeAs(ChallengeIdeas.poolFor(activity, hour, Language.PT));
        }
    }

    @Test
    void picksDuskIdeasForEveningWindows() {
        OutdoorWindow evening = window(LocalDateTime.of(2026, 10, 8, 17, 0));
        String idea = ChallengeIdeas.pick(Activity.PICNIC, evening, TestLocations.CAMPINAS, Language.EN);

        assertThat(ChallengeIdeas.poolFor(Activity.PICNIC, 17, Language.EN)).contains(idea);
    }

    private static OutdoorWindow window(LocalDateTime start) {
        return new OutdoorWindow(start, start.plusHours(1), 90, 20.0, 1.0, 0, 5.0);
    }
}
