package io.quarkus.infra.performance.graphics.charts.scales;

/**
 * Utility class for calculating nice intervals for chart scales.
 * A "nice interval" is a round number (1, 2, 5, 10, 20, 50, 100, 200, 500, etc.)
 * that provides visually pleasing tick marks on charts.
 */
public class IntervalCalculator {

    public static final int TARGET_NUMBER_OF_INTERVALS = 7;

    private IntervalCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates a nice interval for a given maximum value.
     * The interval will be a round number that divides the range into approximately 6 segments.
     * For negative values, the absolute value is used for calculation.
     *
     * @param max the maximum value for the scale
     * @return a nice interval value (1, 2, 5, 10, 20, 50, 100, 200, 500, etc.)
     */
    public static double getNiceInterval(double max) {
        // Work with absolute value to handle negative numbers
        double absMax = Math.abs(max);

        if (absMax == 0.0) {
            return 1;
        }

        // Round to a nice number (1, 2, 5, 10, 20, 50, 100, 200, 500, etc.)
        double roughInterval = absMax / TARGET_NUMBER_OF_INTERVALS; // Aim for a sensible number of intervals
        int pow = 10;
        double magnitude = Math.pow(pow, Math.floor(Math.log10(roughInterval)));
        double normalizedInterval = roughInterval / magnitude;

        double niceInterval;
        if (normalizedInterval <= 1) {
            niceInterval = magnitude;
        } else if (normalizedInterval <= 2) {
            niceInterval = 2 * magnitude;
        } else if (normalizedInterval <= 5) {
            niceInterval = 5 * magnitude;
        } else {
            niceInterval = pow * magnitude;
        }
        return niceInterval;
    }
}


