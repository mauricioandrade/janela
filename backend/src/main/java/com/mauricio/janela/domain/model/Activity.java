package com.mauricio.janela.domain.model;

/**
 * Outdoor activities with their comfort limits. Temperatures refer to the apparent (feels-like) temperature.
 */
public enum Activity {

    RUN(10, 22, 5, 30),
    WALK(14, 26, 6, 35),
    BIKE(12, 24, 6, 22),
    PICNIC(18, 28, 4, 20),
    GARDENING(15, 27, 5, 30),
    /** Outdoor gyms in squares and parks, calisthenics: high effort, so heat weighs like running. */
    WORKOUT(10, 23, 5, 30);

    private static final double WARM_MARGIN_C = 4;

    private final double minApparentTempC;
    private final double maxApparentTempC;
    private final double maxComfortableUv;
    private final double maxWindKmh;

    Activity(double minApparentTempC, double maxApparentTempC, double maxComfortableUv, double maxWindKmh) {
        this.minApparentTempC = minApparentTempC;
        this.maxApparentTempC = maxApparentTempC;
        this.maxComfortableUv = maxComfortableUv;
        this.maxWindKmh = maxWindKmh;
    }

    /**
     * Below the range is cool, inside it pleasant, up to {@value #WARM_MARGIN_C} °C above it warm, beyond that hot.
     */
    public Comfort comfortOf(double apparentTempC) {
        if (apparentTempC < minApparentTempC) {
            return Comfort.COOL;
        }
        if (apparentTempC <= maxApparentTempC) {
            return Comfort.PLEASANT;
        }
        return apparentTempC <= maxApparentTempC + WARM_MARGIN_C ? Comfort.WARM : Comfort.HOT;
    }

    public double minApparentTempC() {
        return minApparentTempC;
    }

    public double maxApparentTempC() {
        return maxApparentTempC;
    }

    public double maxComfortableUv() {
        return maxComfortableUv;
    }

    public double maxWindKmh() {
        return maxWindKmh;
    }
}
