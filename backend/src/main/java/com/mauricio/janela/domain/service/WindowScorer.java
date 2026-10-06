package com.mauricio.janela.domain.service;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Deterministic scoring of forecast hours and windows. Pure domain: no framework, no I/O.
 */
public class WindowScorer {

    public static final int MAX_WINDOWS = 3;
    public static final double MIN_HOUR_SCORE = 40;

    private static final double RAIN_PENALTY_PER_PERCENT = 0.6;
    private static final double TEMP_PENALTY_PER_DEGREE = 4;
    private static final double UV_PENALTY_PER_UNIT = 8;
    private static final double WIND_PENALTY_PER_KMH = 2;

    /**
     * Top {@value #MAX_WINDOWS} non-overlapping daytime windows covering {@code durationMinutes}
     * (rounded up to whole hours), best first. Windows containing any hour scoring below
     * {@value #MIN_HOUR_SCORE} are discarded. Across several days, each day's best window is picked before
     * a second window from the same day, so the options are spread out rather than stacked on one morning.
     */
    public List<OutdoorWindow> findBestWindows(List<HourlyForecast> forecast, Activity activity, int durationMinutes) {
        Objects.requireNonNull(activity, "activity");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
        int hours = Math.ceilDiv(durationMinutes, 60);

        List<HourlyForecast> sorted = forecast.stream()
                .filter(hour -> hour.time() != null)
                .sorted(Comparator.comparing(HourlyForecast::time))
                .toList();

        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i + hours <= sorted.size(); i++) {
            List<HourlyForecast> block = sorted.subList(i, i + hours);
            if (isConsecutiveDaytime(block)) {
                scoreBlock(block, activity).ifPresent(candidates::add);
            }
        }

        candidates.sort(Comparator.comparingDouble(Candidate::averageScore).reversed()
                .thenComparing(candidate -> candidate.window().start()));

        List<OutdoorWindow> selected = new ArrayList<>();
        Set<LocalDate> daysWithAWindow = new HashSet<>();
        for (Candidate candidate : candidates) {
            if (daysWithAWindow.add(candidate.window().start().toLocalDate())) {
                select(candidate, selected);
            }
        }
        for (Candidate candidate : candidates) {
            select(candidate, selected);
        }
        // Back to rank order: best first, earliest first on ties.
        return candidates.stream().map(Candidate::window).filter(selected::contains).toList();
    }

    /**
     * Hour score in [0, 100]. Starts at 100 and subtracts penalties for rain, temperature outside the
     * activity's ideal range, UV and wind above the activity's limits. Null values add no penalty.
     */
    public double scoreHour(HourlyForecast hour, Activity activity) {
        double score = 100;

        if (hour.precipitationProbability() != null) {
            score -= hour.precipitationProbability() * RAIN_PENALTY_PER_PERCENT;
        }
        if (hour.apparentTemperatureC() != null) {
            double temp = hour.apparentTemperatureC();
            double degreesOutside = Math.max(0, activity.minApparentTempC() - temp)
                    + Math.max(0, temp - activity.maxApparentTempC());
            score -= degreesOutside * TEMP_PENALTY_PER_DEGREE;
        }
        if (hour.uvIndex() != null) {
            score -= Math.max(0, hour.uvIndex() - activity.maxComfortableUv()) * UV_PENALTY_PER_UNIT;
        }
        if (hour.windSpeedKmh() != null) {
            score -= Math.max(0, hour.windSpeedKmh() - activity.maxWindKmh()) * WIND_PENALTY_PER_KMH;
        }

        return Math.clamp(score, 0, 100);
    }

    private static void select(Candidate candidate, List<OutdoorWindow> selected) {
        if (selected.size() < MAX_WINDOWS
                && selected.stream().noneMatch(chosen -> chosen.overlaps(candidate.window()))) {
            selected.add(candidate.window());
        }
    }

    private boolean isConsecutiveDaytime(List<HourlyForecast> block) {
        for (int i = 0; i < block.size(); i++) {
            if (Boolean.FALSE.equals(block.get(i).isDay())) {
                return false;
            }
            if (i > 0 && !block.get(i).time().equals(block.get(i - 1).time().plusHours(1))) {
                return false;
            }
        }
        return true;
    }

    private Optional<Candidate> scoreBlock(List<HourlyForecast> block, Activity activity) {
        double total = 0;
        for (HourlyForecast hour : block) {
            double hourScore = scoreHour(hour, activity);
            if (hourScore < MIN_HOUR_SCORE) {
                return Optional.empty();
            }
            total += hourScore;
        }
        double average = total / block.size();

        OutdoorWindow window = new OutdoorWindow(
                block.getFirst().time(),
                block.getLast().time().plusHours(1),
                (int) Math.round(average),
                averageApparentTemp(block),
                max(block, HourlyForecast::uvIndex),
                max(block, HourlyForecast::precipitationProbability),
                max(block, HourlyForecast::windSpeedKmh));
        return Optional.of(new Candidate(window, average));
    }

    private static Double averageApparentTemp(List<HourlyForecast> block) {
        var stats = block.stream()
                .map(HourlyForecast::apparentTemperatureC)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        return stats.getCount() == 0 ? null : Math.round(stats.getAverage() * 10) / 10.0;
    }

    private static <T extends Comparable<T>> T max(List<HourlyForecast> block, Function<HourlyForecast, T> value) {
        return block.stream()
                .map(value)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private record Candidate(OutdoorWindow window, double averageScore) {
    }
}
