package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.charts.scales.IntervalCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for IntervalCalculator class that calculates nice intervals for chart scales.
 * A "nice interval" is a round number (1, 2, 5, 10, 20, 50, 100, 200, 500, etc.)
 * that provides visually pleasing tick marks on charts.
 */
public class IntervalCalculatorTest {

    @Test
    public void testGetNiceIntervalWithZero() {
        // Zero should return 1 as a default interval
        assertEquals(1.0, IntervalCalculator.getNiceInterval(0.0));
    }

    @Test
    public void testGetNiceIntervalWithNegativeValue() {
        // Negative values should work with absolute value
        // For max = -6, abs = 6, roughInterval = 1, should return 1
        assertEquals(1.0, IntervalCalculator.getNiceInterval(- 6.0));

        // For max = -60, abs = 60, roughInterval = 10, should return 10
        assertEquals(10.0, IntervalCalculator.getNiceInterval(- 60.0));

        // For max = -1000, abs = 1000, roughInterval = 166.67, should return 200
        assertEquals(200.0, IntervalCalculator.getNiceInterval(- 1000.0));
    }

    @Test
    public void testGetNiceIntervalSmallValues() {
        // For max = 6, roughInterval = 1, should return 1
        assertEquals(1.0, IntervalCalculator.getNiceInterval(6.0));

        // For max = 12, roughInterval = 2, should return 2
        assertEquals(2.0, IntervalCalculator.getNiceInterval(12.0));

        // For max = 30, roughInterval = 5, should return 5
        assertEquals(5.0, IntervalCalculator.getNiceInterval(30.0));
    }

    @Test
    public void testGetNiceIntervalMediumValues() {
        // For max = 60, roughInterval = 10, should return 10
        assertEquals(10.0, IntervalCalculator.getNiceInterval(60.0));

        // For max = 120, roughInterval = 20, should return 20
        assertEquals(20.0, IntervalCalculator.getNiceInterval(120.0));

        // For max = 300, roughInterval = 50, should return 50
        assertEquals(50.0, IntervalCalculator.getNiceInterval(300.0));
    }

    @Test
    public void testGetNiceIntervalLargeValues() {
        // For max = 600, roughInterval = 100, should return 100
        assertEquals(100.0, IntervalCalculator.getNiceInterval(600.0));

        // For max = 1200, roughInterval = 200, should return 200
        assertEquals(200.0, IntervalCalculator.getNiceInterval(1200.0));

        // For max = 3000, roughInterval = 500, should return 500
        assertEquals(500.0, IntervalCalculator.getNiceInterval(3000.0));
    }

    @Test
    public void testGetNiceIntervalVeryLargeValues() {
        // For max = 6000, roughInterval = 1000, should return 1000
        assertEquals(1000.0, IntervalCalculator.getNiceInterval(6000.0));

        // For max = 6000, roughInterval = 1000, should return 1000
        assertEquals(1000.0, IntervalCalculator.getNiceInterval(6300.0));

        // For max = 12000, roughInterval = 2000, should return 2000
        assertEquals(2000.0, IntervalCalculator.getNiceInterval(12000.0));

        // For max = 30000, roughInterval = 5000, should return 5000
        assertEquals(5000.0, IntervalCalculator.getNiceInterval(30000.0));
    }

    @Test
    public void testGetNiceIntervalBoundaryValues() {
        // Test values that fall on boundaries between nice intervals

        assertEquals(1.0, IntervalCalculator.getNiceInterval(7.0));

        assertEquals(2.0, IntervalCalculator.getNiceInterval(13.0));

        // max = 31 -> roughInterval = 5.166... -> normalized = 5.166... -> should return 10
        assertEquals(5.0, IntervalCalculator.getNiceInterval(31.0));
    }

    @Test
    public void testGetNiceIntervalEdgeCases() {
        // Very small positive value
        assertEquals(0.02, IntervalCalculator.getNiceInterval(0.1));

        // Value just above zero
        assertEquals(0.0002, IntervalCalculator.getNiceInterval(0.001));

        // Value of 1
        assertEquals(0.2, IntervalCalculator.getNiceInterval(1.0));
    }

    @Test
    public void testGetNiceIntervalRealWorldExamples() {
        // Typical chart scenarios

        // Time in milliseconds (0-1000ms)
        assertEquals(200.0, IntervalCalculator.getNiceInterval(1000.0));

        // Time in seconds (0-60s)
        assertEquals(10.0, IntervalCalculator.getNiceInterval(60.0));

        // Throughput (0-5000 req/s)
        assertEquals(1000.0, IntervalCalculator.getNiceInterval(5000.0));

        // Memory in MB (0-512MB)
        assertEquals(100.0, IntervalCalculator.getNiceInterval(512.0));

        // Large throughput (0-50000 req/s)
        assertEquals(10000.0, IntervalCalculator.getNiceInterval(50000.0));
    }

    @Test
    public void testGetNiceIntervalConsistency() {
        // Test that similar values produce consistent intervals

        // Values around 100 should all produce interval of 20
        assertEquals(20.0, IntervalCalculator.getNiceInterval(100.0));
        assertEquals(20.0, IntervalCalculator.getNiceInterval(105.0));
        assertEquals(20.0, IntervalCalculator.getNiceInterval(110.0));

        // Values around 1000 should all produce interval of 200
        assertEquals(200.0, IntervalCalculator.getNiceInterval(1000.0));
        assertEquals(200.0, IntervalCalculator.getNiceInterval(1050.0));
        assertEquals(200.0, IntervalCalculator.getNiceInterval(1100.0));
    }

    @Test
    public void testGetNiceIntervalProducesApproximatelySixIntervals() {
        // The algorithm aims for approximately 6 intervals
        // Verify that the returned interval divides the max into roughly 5-7 segments

        double max = 1000.0;
        double interval = IntervalCalculator.getNiceInterval(max);
        double numIntervals = max / interval;
        // Should be between 4 and 8 intervals (allowing some flexibility)
        assertEquals(true, numIntervals >= 4.0 && numIntervals <= 8.0,
                "Expected 4-8 intervals, got " + numIntervals);

        max = 5000.0;
        interval = IntervalCalculator.getNiceInterval(max);
        numIntervals = max / interval;
        assertEquals(true, numIntervals >= 4.0 && numIntervals <= 8.0,
                "Expected 4-8 intervals, got " + numIntervals);
    }

    @Test
    public void testGetNiceIntervalReturnsNiceNumbers() {
        // Verify that all returned intervals are "nice" numbers
        // (multiples of 1, 2, or 5 times a power of 10)

        double[] testValues = {10, 50, 100, 500, 1000, 5000, 10000, 50000};

        for (double max : testValues) {
            double interval = IntervalCalculator.getNiceInterval(max);

            // Check if interval is a nice number by verifying it's a multiple of
            // 1, 2, or 5 times a power of 10
            double magnitude = Math.pow(10, Math.floor(Math.log10(interval)));
            double normalized = interval / magnitude;

            boolean isNice = (normalized == 1.0 || normalized == 2.0 ||
                    normalized == 5.0 || normalized == 10.0);

            assertEquals(true, isNice,
                    "Interval " + interval + " for max " + max + " is not a nice number");
        }
    }

    @Test
    public void testGetNiceIntervalDecimalValues() {
        // Test with decimal max values
        assertEquals(1.0, IntervalCalculator.getNiceInterval(5.5));
        assertEquals(2.0, IntervalCalculator.getNiceInterval(11.7));
        assertEquals(5.0, IntervalCalculator.getNiceInterval(28.3));
        assertEquals(10.0, IntervalCalculator.getNiceInterval(55.9));
    }

    @Test
    public void testGetNiceIntervalVeryLargeNumbers() {
        // Test with very large numbers
        assertEquals(100000.0, IntervalCalculator.getNiceInterval(600000.0));
        assertEquals(200000.0, IntervalCalculator.getNiceInterval(1200000.0));
        assertEquals(1000000.0, IntervalCalculator.getNiceInterval(6000000.0));
    }
}


