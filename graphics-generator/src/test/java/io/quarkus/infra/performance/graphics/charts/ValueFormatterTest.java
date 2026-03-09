package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.charts.scales.ValueFormatter;
import io.quarkus.infra.performance.graphics.model.units.Milliseconds;
import io.quarkus.infra.performance.graphics.model.units.Seconds;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for ValueFormatter class that formats numeric values for display in charts.
 * The formatter adapts its output based on the maximum value in the scale.
 */
public class ValueFormatterTest {

    @Test
    public void testFormatZeroWithSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(1000));
        assertEquals("0", formatter.format(new Seconds(0)));
    }

    @Test
    public void testFormatZeroWithTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(1000));
        assertEquals("0", formatter.format(new TransactionsPerSecond(0)));
    }

    @Test
    public void testFormatSmallScaleWithWholeNumbersSeconds() {
        // When max is small (< 5000), use full numbers
        ValueFormatter formatter = new ValueFormatter(new Seconds(1500));
        assertEquals("0", formatter.format(new Seconds(0)));
        assertEquals("500", formatter.format(new Seconds(500)));
        assertEquals("1000", formatter.format(new Seconds(1000)));
        assertEquals("1500", formatter.format(new Seconds(1500)));
    }

    @Test
    public void testFormatSmallScaleWithWholeNumbersTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(1500));
        assertEquals("0", formatter.format(new TransactionsPerSecond(0)));
        assertEquals("500", formatter.format(new TransactionsPerSecond(500)));
        assertEquals("1000", formatter.format(new TransactionsPerSecond(1000)));
        assertEquals("1500", formatter.format(new TransactionsPerSecond(1500)));
    }

    @Test
    public void testFormatSmallScaleWithDecimalsSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(1500));
        assertEquals("500.5", formatter.format(new Seconds(500.5)));
        assertEquals("1001", formatter.format(new Seconds(1000.5)));
    }

    @Test
    public void testFormatSmallScaleWithDecimalsTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(1500));
        assertEquals("500.5", formatter.format(new TransactionsPerSecond(500.5)));
        assertEquals("1001", formatter.format(new TransactionsPerSecond(1000.5)));
    }

    @Test
    public void testFormatLargeScaleUsesKSuffixSeconds() {
        // When max is large (>= 5000), use k suffix
        ValueFormatter formatter = new ValueFormatter(new Seconds(8000));
        assertEquals("0", formatter.format(new Seconds(0)));
        assertEquals("1k", formatter.format(new Seconds(1000)));
        assertEquals("2k", formatter.format(new Seconds(2000)));
        assertEquals("5k", formatter.format(new Seconds(5000)));
        assertEquals("8k", formatter.format(new Seconds(8000)));
    }

    @Test
    public void testFormatLargeScaleUsesKSuffixTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(8000));
        assertEquals("0", formatter.format(new TransactionsPerSecond(0)));
        assertEquals("1k", formatter.format(new TransactionsPerSecond(1000)));
        assertEquals("2k", formatter.format(new TransactionsPerSecond(2000)));
        assertEquals("5k", formatter.format(new TransactionsPerSecond(5000)));
        assertEquals("8k", formatter.format(new TransactionsPerSecond(8000)));
    }

    @Test
    public void testFormatMillisecondsConvertsToSecondsWhenLarge() {
        // When max milliseconds >= 2000, convert to seconds
        ValueFormatter formatter = new ValueFormatter(new Milliseconds(3000));
        assertEquals("0", formatter.format(new Milliseconds(0)));
        assertEquals("1s", formatter.format(new Milliseconds(1000)));
        assertEquals("2s", formatter.format(new Milliseconds(2000)));
        assertEquals("3s", formatter.format(new Milliseconds(3000)));
    }

    @Test
    public void testFormatMillisecondsWithDecimalsConvertsToSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Milliseconds(3000));
        assertEquals("1.5s", formatter.format(new Milliseconds(1500)));
        assertEquals("2.3s", formatter.format(new Milliseconds(2300)));
    }

    @Test
    public void testFormatMillisecondsSmallScaleNoConversion() {
        // When max < 2000ms, don't convert to seconds
        ValueFormatter formatter = new ValueFormatter(new Milliseconds(1500));
        assertEquals("0", formatter.format(new Milliseconds(0)));
        assertEquals("500", formatter.format(new Milliseconds(500)));
        assertEquals("1000", formatter.format(new Milliseconds(1000)));
        assertEquals("1500", formatter.format(new Milliseconds(1500)));
    }

    @Test
    public void testFormatMillisecondsBoundaryAt2000() {
        // Test the boundary where we switch to seconds
        ValueFormatter formatterBelow = new ValueFormatter(new Milliseconds(1999));
        assertEquals("1000", formatterBelow.format(new Milliseconds(1000)));

        ValueFormatter formatterAt = new ValueFormatter(new Milliseconds(2000));
        assertEquals("1s", formatterAt.format(new Milliseconds(1000)));
        assertEquals("2s", formatterAt.format(new Milliseconds(2000)));
    }

    @Test
    public void testFormatLargeScaleWithDecimalsUsesKSuffixSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(8000));
        assertEquals("1.5k", formatter.format(new Seconds(1500)));
        assertEquals("2.3k", formatter.format(new Seconds(2300)));
        assertEquals("5.7k", formatter.format(new Seconds(5700)));
    }

    @Test
    public void testFormatLargeScaleWithDecimalsUsesKSuffixTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(8000));
        assertEquals("1.5k", formatter.format(new TransactionsPerSecond(1500)));
        assertEquals("2.3k", formatter.format(new TransactionsPerSecond(2300)));
        assertEquals("5.7k", formatter.format(new TransactionsPerSecond(5700)));
    }

    @Test
    public void testFormatVeryLargeScaleUsesKSuffixSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(50000));
        assertEquals("0", formatter.format(new Seconds(0)));
        assertEquals("10k", formatter.format(new Seconds(10000)));
        assertEquals("25k", formatter.format(new Seconds(25000)));
        assertEquals("50k", formatter.format(new Seconds(50000)));
    }

    @Test
    public void testFormatVeryLargeScaleUsesKSuffixTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(50000));
        assertEquals("0", formatter.format(new TransactionsPerSecond(0)));
        assertEquals("10k", formatter.format(new TransactionsPerSecond(10000)));
        assertEquals("25k", formatter.format(new TransactionsPerSecond(25000)));
        assertEquals("50k", formatter.format(new TransactionsPerSecond(50000)));
    }

    @Test
    public void testFormatVeryLargeScaleWithDecimalsUsesKSuffixSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(50000));
        assertEquals("10.5k", formatter.format(new Seconds(10500)));
        assertEquals("25.3k", formatter.format(new Seconds(25300)));
    }

    @Test
    public void testFormatVeryLargeScaleWithDecimalsUsesKSuffixTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(50000));
        assertEquals("10.5k", formatter.format(new TransactionsPerSecond(10500)));
        assertEquals("25.3k", formatter.format(new TransactionsPerSecond(25300)));
    }

    @Test
    public void testFormatSmallNumbersOnLargeScaleSeconds() {
        // Even on large scale, small numbers should be formatted appropriately
        ValueFormatter formatter = new ValueFormatter(new Seconds(10000));
        assertEquals("0", formatter.format(new Seconds(0)));
        assertEquals("100", formatter.format(new Seconds(100)));
        assertEquals("500", formatter.format(new Seconds(500)));
    }

    @Test
    public void testFormatSmallNumbersOnLargeScaleTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(10000));
        assertEquals("0", formatter.format(new TransactionsPerSecond(0)));
        assertEquals("100", formatter.format(new TransactionsPerSecond(100)));
        assertEquals("500", formatter.format(new TransactionsPerSecond(500)));
    }

    @Test
    public void testFormatBoundaryAt5000Seconds() {
        // Test the boundary where we switch to k suffix
        ValueFormatter formatterBelow = new ValueFormatter(new Seconds(4999));
        assertEquals("1000", formatterBelow.format(new Seconds(1000)));
        assertEquals("2000", formatterBelow.format(new Seconds(2000)));

        ValueFormatter formatterAt = new ValueFormatter(new Seconds(5000));
        assertEquals("1k", formatterAt.format(new Seconds(1000)));
        assertEquals("2k", formatterAt.format(new Seconds(2000)));
        assertEquals("5k", formatterAt.format(new Seconds(5000)));
    }

    @Test
    public void testFormatBoundaryAt5000Transactions() {
        ValueFormatter formatterBelow = new ValueFormatter(new TransactionsPerSecond(4999));
        assertEquals("1000", formatterBelow.format(new TransactionsPerSecond(1000)));
        assertEquals("2000", formatterBelow.format(new TransactionsPerSecond(2000)));

        ValueFormatter formatterAt = new ValueFormatter(new TransactionsPerSecond(5000));
        assertEquals("1k", formatterAt.format(new TransactionsPerSecond(1000)));
        assertEquals("2k", formatterAt.format(new TransactionsPerSecond(2000)));
        assertEquals("5k", formatterAt.format(new TransactionsPerSecond(5000)));
    }

    @Test
    public void testFormatNegativeNumbersSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(8000));
        assertEquals("-1k", formatter.format(new Seconds(- 1000)));
        assertEquals("-2.5k", formatter.format(new Seconds(- 2500)));
    }

    @Test
    public void testFormatNegativeNumbersTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(8000));
        assertEquals("-1k", formatter.format(new TransactionsPerSecond(- 1000)));
        assertEquals("-2.5k", formatter.format(new TransactionsPerSecond(- 2500)));
    }

    @Test
    public void testFormatVerySmallNumbersSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(1000));
        assertEquals("0.1", formatter.format(new Seconds(0.1)));
        assertEquals("0.5", formatter.format(new Seconds(0.5)));
        assertEquals("5.5", formatter.format(new Seconds(5.5)));
    }

    @Test
    public void testFormatVerySmallNumbersTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(1000));
        assertEquals("0.1", formatter.format(new TransactionsPerSecond(0.1)));
        assertEquals("0.5", formatter.format(new TransactionsPerSecond(0.5)));
        assertEquals("5.5", formatter.format(new TransactionsPerSecond(5.5)));
    }

    @Test
    public void testFormatRoundingInKFormatSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(10000));
        // 1234 -> 1.2k (rounded to 1 decimal)
        assertEquals("1.2k", formatter.format(new Seconds(1234)));
        // 1250 -> 1.3k (rounded up)
        assertEquals("1.3k", formatter.format(new Seconds(1250)));
        // 1999 -> 2.0k (rounded)
        assertEquals("2.0k", formatter.format(new Seconds(1999)));
    }

    @Test
    public void testFormatRoundingInKFormatTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(10000));
        assertEquals("1.2k", formatter.format(new TransactionsPerSecond(1234)));
        assertEquals("1.3k", formatter.format(new TransactionsPerSecond(1250)));
        assertEquals("2.0k", formatter.format(new TransactionsPerSecond(1999)));
    }

    @Test
    public void testFormatWholeThousandsInKFormatSeconds() {
        ValueFormatter formatter = new ValueFormatter(new Seconds(10000));
        // Whole thousands should not show decimal
        assertEquals("1k", formatter.format(new Seconds(1000)));
        assertEquals("2k", formatter.format(new Seconds(2000)));
        assertEquals("10k", formatter.format(new Seconds(10000)));
    }

    @Test
    public void testFormatWholeThousandsInKFormatTransactions() {
        ValueFormatter formatter = new ValueFormatter(new TransactionsPerSecond(10000));
        assertEquals("1k", formatter.format(new TransactionsPerSecond(1000)));
        assertEquals("2k", formatter.format(new TransactionsPerSecond(2000)));
        assertEquals("10k", formatter.format(new TransactionsPerSecond(10000)));
    }
}


