package com.mauricio.janela.domain.service;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.OutdoorWindow;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class WindowScorerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 6);

    private final WindowScorer scorer = new WindowScorer();

    @Test
    void heavyRainAt17hExcludesEveryWindowCoveringThatHour() {
        List<HourlyForecast> forecast = new ArrayList<>();
        for (int h = 6; h <= 18; h++) {
            int rain = h == 17 ? 95 : 0;
            forecast.add(hour(h, 18.0, 2.0, rain, 8.0, true));
        }

        List<OutdoorWindow> windows = scorer.findBestWindows(forecast, Activity.RUN, 120);

        LocalDateTime storm = at(17);
        assertThat(windows).isNotEmpty();
        assertThat(windows).noneMatch(w -> !w.start().isAfter(storm) && w.end().isAfter(storm));
        assertThat(windows).noneMatch(w -> w.start().equals(at(16)));
    }

    @Test
    void highUvAtNoonMakesRunPreferMorningAndPicnicPreferLateAfternoon() {
        List<HourlyForecast> forecast = tropicalSunnyDay();

        OutdoorWindow bestRun = scorer.findBestWindows(forecast, Activity.RUN, 60).getFirst();
        OutdoorWindow bestPicnic = scorer.findBestWindows(forecast, Activity.PICNIC, 120).getFirst();

        assertThat(bestRun.start().getHour()).isLessThan(10);
        assertThat(bestPicnic.start().getHour()).isGreaterThanOrEqualTo(15);
    }

    @Test
    void nightHoursNeverEnterAWindow() {
        List<HourlyForecast> forecast = new ArrayList<>();
        for (int h = 0; h <= 23; h++) {
            boolean isDay = h >= 6 && h <= 17;
            // Perfect conditions at night, mediocre during the day: night must still be ignored.
            forecast.add(isDay
                    ? hour(h, 27.0, 6.0, 20, 10.0, true)
                    : hour(h, 18.0, 0.0, 0, 5.0, false));
        }

        List<OutdoorWindow> windows = scorer.findBestWindows(forecast, Activity.RUN, 60);

        assertThat(windows).isNotEmpty();
        assertThat(windows).allSatisfy(w -> {
            assertThat(w.start().getHour()).isBetween(6, 17);
            assertThat(w.end()).isBeforeOrEqualTo(at(18));
        });
    }

    @Test
    void returnsTopThreeWithoutOverlap() {
        List<HourlyForecast> forecast = new ArrayList<>();
        for (int h = 6; h <= 17; h++) {
            forecast.add(hour(h, 18.0, 1.0, 0, 5.0, true));
        }

        List<OutdoorWindow> windows = scorer.findBestWindows(forecast, Activity.WALK, 120);

        assertThat(windows).hasSize(WindowScorer.MAX_WINDOWS);
        for (int i = 0; i < windows.size(); i++) {
            for (int j = i + 1; j < windows.size(); j++) {
                assertThat(windows.get(i).overlaps(windows.get(j)))
                        .as("%s overlaps %s", windows.get(i), windows.get(j))
                        .isFalse();
            }
        }
    }

    @Nested
    class HourScore {

        @Test
        void perfectHourScores100() {
            assertThat(scorer.scoreHour(hour(7, 16.0, 1.0, 0, 5.0, true), Activity.RUN)).isEqualTo(100);
        }

        @Test
        void appliesEachPenaltyFromTheSpec() {
            // RUN: 10–22 °C, UV 5, wind 30 km/h
            HourlyForecast hour = hour(12, 24.0, 7.0, 60, 33.0, true);

            double expected = 100
                    - 60 * 0.6   // rain
                    - 2 * 4      // 2 °C above the range
                    - 2 * 8      // 2 UV units above the limit
                    - 3 * 2;     // 3 km/h above the limit

            assertThat(scorer.scoreHour(hour, Activity.RUN)).isCloseTo(expected, within(1e-9));
        }

        @Test
        void penalizesColdBelowTheRange() {
            assertThat(scorer.scoreHour(hour(6, 7.0, 0.0, 0, 0.0, true), Activity.RUN)).isEqualTo(88);
        }

        @Test
        void clampsAtZero() {
            assertThat(scorer.scoreHour(hour(13, 40.0, 12.0, 100, 60.0, true), Activity.PICNIC)).isZero();
        }

        @Test
        void nullValuesAreNeutral() {
            HourlyForecast unknown = new HourlyForecast(at(9), null, null, null, null, null, null);

            assertThat(scorer.scoreHour(unknown, Activity.BIKE)).isEqualTo(100);
        }
    }

    @Nested
    class Windows {

        @Test
        void discardsWindowWhenAnyHourScoresBelow40() {
            List<HourlyForecast> forecast = List.of(
                    hour(8, 18.0, 1.0, 0, 5.0, true),
                    hour(9, 18.0, 1.0, 0, 5.0, true),
                    hour(10, 18.0, 1.0, 100, 5.0, true)); // 100 - 60 = 40, still allowed

            List<OutdoorWindow> withBorderline = scorer.findBestWindows(forecast, Activity.RUN, 180);
            assertThat(withBorderline).hasSize(1);

            List<HourlyForecast> withStorm = List.of(
                    hour(8, 18.0, 1.0, 0, 5.0, true),
                    hour(9, 18.0, 1.0, 0, 5.0, true),
                    hour(10, 18.0, 1.0, 100, 45.0, true)); // 100 - 60 - 30 = 10

            assertThat(scorer.findBestWindows(withStorm, Activity.RUN, 180)).isEmpty();
        }

        @Test
        void roundsDurationUpToWholeHours() {
            List<HourlyForecast> forecast = new ArrayList<>();
            for (int h = 6; h <= 9; h++) {
                forecast.add(hour(h, 18.0, 1.0, 0, 5.0, true));
            }

            OutdoorWindow window = scorer.findBestWindows(forecast, Activity.RUN, 90).getFirst();

            assertThat(window.start()).isEqualTo(at(6));
            assertThat(window.end()).isEqualTo(at(8));
        }

        @Test
        void requiresConsecutiveHours() {
            List<HourlyForecast> forecast = List.of(
                    hour(8, 18.0, 1.0, 0, 5.0, true),
                    hour(10, 18.0, 1.0, 0, 5.0, true));

            assertThat(scorer.findBestWindows(forecast, Activity.RUN, 120)).isEmpty();
        }

        @Test
        void aggregatesWindowNumbers() {
            List<HourlyForecast> forecast = List.of(
                    hour(7, 17.0, 0.5, 10, 6.0, true),
                    hour(8, 19.0, 2.0, 5, 9.0, true));

            OutdoorWindow window = scorer.findBestWindows(forecast, Activity.RUN, 120).getFirst();

            assertThat(window.score()).isEqualTo(96); // (94 + 97) / 2 = 95.5, rounded
            assertThat(window.apparentTempC()).isEqualTo(18.0);
            assertThat(window.maxUv()).isEqualTo(2.0);
            assertThat(window.maxRainProbability()).isEqualTo(10);
            assertThat(window.maxWindKmh()).isEqualTo(9.0);
        }

        @Test
        void spreadsOptionsAcrossDaysBeforeRepeatingOne() {
            // Day 1: room for three perfect windows (06–12). Day 2: only 06–08 is good, and a little worse.
            List<HourlyForecast> forecast = new ArrayList<>();
            for (int h = 6; h < 12; h++) {
                forecast.add(hour(h, 18.0, 1.0, 0, 5.0, true));
            }
            forecast.add(new HourlyForecast(DAY.plusDays(1).atTime(6, 0), 18.0, 18.0, 20, 1.0, 5.0, true));
            forecast.add(new HourlyForecast(DAY.plusDays(1).atTime(7, 0), 18.0, 18.0, 20, 1.0, 5.0, true));

            List<OutdoorWindow> windows = scorer.findBestWindows(forecast, Activity.RUN, 120);

            assertThat(windows).extracting(OutdoorWindow::start)
                    .containsExactly(at(6), at(8), DAY.plusDays(1).atTime(6, 0));
        }

        @Test
        void keepsSeveralWindowsOnTheSameDayWhenThereIsOnlyOneDay() {
            List<HourlyForecast> forecast = new ArrayList<>();
            for (int h = 6; h < 12; h++) {
                forecast.add(hour(h, 18.0, 1.0, 0, 5.0, true));
            }

            assertThat(scorer.findBestWindows(forecast, Activity.RUN, 120)).extracting(OutdoorWindow::start)
                    .containsExactly(at(6), at(8), at(10));
        }

        @Test
        void rejectsNonPositiveDuration() {
            assertThatThrownBy(() -> scorer.findBestWindows(List.of(), Activity.RUN, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    /**
     * Clear-sky tropical day: cool early morning, UV 11 around noon, warm but gentle late afternoon.
     */
    private static List<HourlyForecast> tropicalSunnyDay() {
        double[][] tempAndUv = {
                {15, 0.2}, {16, 0.8}, {17, 2}, {20, 5}, {24, 6}, {27, 8}, {30, 11},
                {31, 11}, {31, 9}, {30, 7}, {28, 4}, {26, 2}, {24, 0.5}, {22, 0}};
        List<HourlyForecast> forecast = new ArrayList<>();
        for (int i = 0; i < tempAndUv.length; i++) {
            int h = 6 + i;
            forecast.add(hour(h, tempAndUv[i][0], tempAndUv[i][1], 0, 8.0, h <= 18));
        }
        return forecast;
    }

    private static HourlyForecast hour(int hour, double apparentTemp, double uv, int rain, double wind, boolean isDay) {
        return new HourlyForecast(at(hour), apparentTemp, apparentTemp, rain, uv, wind, isDay);
    }

    private static LocalDateTime at(int hour) {
        return DAY.atTime(hour, 0);
    }
}
