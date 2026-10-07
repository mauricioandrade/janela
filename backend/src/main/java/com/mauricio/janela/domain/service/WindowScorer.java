package com.mauricio.janela.domain.service;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.HourScore;
import com.mauricio.janela.domain.model.HourlyForecast;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Deterministic scoring of forecast hours and windows. Pure domain: no framework, no I/O.
 */
public class WindowScorer {

    public static final int WINDOWS_TO_OFFER = 3;
    public static final double MIN_HOUR_SCORE = 40;

    private static final double RAIN_PENALTY_PER_PERCENT = 0.6;
    private static final double TEMP_PENALTY_PER_DEGREE = 4;
    private static final double UV_PENALTY_PER_UNIT = 8;
    private static final double WIND_PENALTY_PER_KMH = 2;

    /**
     * The best non-overlapping daytime windows covering {@code durationMinutes} (rounded up to whole hours),
     * best first. Windows containing any hour scoring below {@value #MIN_HOUR_SCORE} are discarded. Every day
     * in the forecast gets the same share of about {@value #WINDOWS_TO_OFFER} options — three on one day, two
     * a day over two days, one a day over three — so no day shows up more often than another.
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

        long days = sorted.stream()
                .filter(hour -> !Boolean.FALSE.equals(hour.isDay()))
                .map(hour -> hour.time().toLocalDate())
                .distinct()
                .count();
        int perDay = (int) Math.ceilDiv(WINDOWS_TO_OFFER, Math.max(days, 1));

        List<OutdoorWindow> selected = new ArrayList<>();
        Map<LocalDate, Integer> windowsPerDay = new HashMap<>();
        for (Candidate candidate : candidates) {
            LocalDate day = candidate.window().start().toLocalDate();
            if (windowsPerDay.getOrDefault(day, 0) < perDay
                    && selected.stream().noneMatch(chosen -> chosen.overlaps(candidate.window()))) {
                selected.add(candidate.window());
                windowsPerDay.merge(day, 1, Integer::sum);
            }
        }
        return List.copyOf(selected);
    }

    /**
     * The rounded score of every daylight hour, in time order; night hours are left out.
     */
    public List<HourScore> scoreDaylight(List<HourlyForecast> forecast, Activity activity) {
        return forecast.stream()
                .filter(hour -> hour.time() != null && !Boolean.FALSE.equals(hour.isDay()))
                .sorted(Comparator.comparing(HourlyForecast::time))
                .map(hour -> new HourScore(hour.time(), (int) Math.round(scoreHour(hour, activity))))
                .toList();
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
