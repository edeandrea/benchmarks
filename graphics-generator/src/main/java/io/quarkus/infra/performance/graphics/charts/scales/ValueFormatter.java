package io.quarkus.infra.performance.graphics.charts.scales;

import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;
import io.quarkus.infra.performance.graphics.model.units.Milliseconds;

/**
 * Utility class for formatting numeric values for display in charts.
 * The formatter adapts its output based on the maximum value in the scale
 */
public class ValueFormatter {
    private static final double K_THRESHOLD = 5000;
    private static final double USE_K_THRESHOLD = 1000;
    private static final double MS_TO_S_THRESHOLD = 2000;

    private final double maxValue;
    private final boolean useKSuffix;
    private final boolean useSecondsForMilliseconds;

    /**
     * Creates a ValueFormatter with the specified maximum value.
     * The max value determines the formatting strategy.
     *
     * @param maxValue the maximum value in the scale
     */
    public ValueFormatter(DimensionalNumber maxValue) {
        this.maxValue = maxValue.getValue();
        this.useSecondsForMilliseconds = maxValue instanceof Milliseconds && this.maxValue >= MS_TO_S_THRESHOLD;
        this.useKSuffix = ! useSecondsForMilliseconds && this.maxValue >= K_THRESHOLD;
    }

    /**
     * Formats a numeric value for display in charts.
     * The formatting depends on the max value provided in the constructor:
     */
    public String format(DimensionalNumber value) {
        return format(value.getValue());
    }

    /**
     * Formats a numeric value for display in charts.
     * The formatting depends on the max value provided in the constructor:
     *
     * @param val the numeric value to format
     * @return the formatted string representation
     */
    public String format(double val) {
        // Handle zero specially
        if (val == 0) {
            return "0";
        }

        // Check if it's a whole number
        boolean isWholeNumber = isWholeValue(val);

        if (useSecondsForMilliseconds) {
            // Convert milliseconds to seconds
            double seconds = val / 1000;
            boolean isWholeSecond = isWholeValue(seconds);

            if (isWholeSecond) {
                return String.format("%ds", (int) seconds);
            } else {
                return String.format("%.1fs", seconds);
            }
        } else if (useKSuffix && Math.abs(val) >= USE_K_THRESHOLD) {
            // Use k suffix for large scales
            double kValue = val / 1000.0;
            boolean isWholeK = isWholeValue(kValue);

            if (isWholeK) {
                return String.format("%dk", (int) kValue);
            } else {
                return String.format("%.1fk", kValue);
            }
        } else if (Math.abs(val) >= USE_K_THRESHOLD) {
            return String.format("%d", Math.round(val));
        } else {
            if (isWholeNumber) {
                return String.format("%d", (int) val);
            } else {
                return String.format("%.1f", val);
            }
        }
    }

    private static boolean isWholeValue(double number) {
        return number == Math.floor(number);
    }
}


