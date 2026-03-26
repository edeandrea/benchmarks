package io.quarkus.infra.performance.graphics.charts.scales;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.Label;
import io.quarkus.infra.performance.graphics.charts.ScaleGroup;
import io.quarkus.infra.performance.graphics.charts.ScaledElement;
import io.quarkus.infra.performance.graphics.charts.Subcanvas;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

/**
 * A scale divider that draws a horizontal line with vertical tick marks and numeric labels,
 * similar to the scale shown in bar charts.
 */
public class ScaleDivider extends ScaledElement {
    private static final int TICK_HEIGHT = 10;
    private static final int LABEL_FONT_SIZE = 14;
    private static final int LINE_THICKNESS = 1;
    private static final int LABEL_PADDING = 6;
    private static final int CIRCLE_DIAMETER = 24;
    private static final int MINIMUM_HEIGHT = TICK_HEIGHT + CIRCLE_DIAMETER + LABEL_PADDING + LINE_THICKNESS;
    private static final int MAXIMUM_HEIGHT = MINIMUM_HEIGHT + 20;
    public static final int NUM_TICKS = 20;

    private final double maxValue;
    private final double[] circleValues;
    private final ValueFormatter formatter;

    /**
     * Creates a scale divider with the specified maximum value and a new ScaleGroup.
     *
     * @param maxValue the maximum value to display on the scale (must be positive)
     * @throws IllegalArgumentException if maxValue is zero or negative
     */
    public ScaleDivider(DimensionalNumber maxValue) {
        this(maxValue, new ScaleGroup());
    }

    /**
     * Creates a scale divider with the specified maximum value and shared ScaleGroup.
     *
     * @param maxValue   the maximum value to display on the scale (must be positive)
     * @param scaleGroup the scale group to use for managing scale
     */
    public ScaleDivider(DimensionalNumber maxValue, ScaleGroup scaleGroup) {
        super(scaleGroup);
        this.maxValue = maxValue.getValue();
        this.circleValues = calculateRoundTickValues(this.maxValue);
        this.formatter = new ValueFormatter(maxValue);
    }

    private double[] calculateRoundTickValues(double max) {

        double niceInterval = IntervalCalculator.getNiceInterval(max);

        // Generate tick values
        java.util.List<Double> ticks = new java.util.ArrayList<>();
        ticks.add(0.0);
        double tick = niceInterval;
        while (tick <= max) {
            ticks.add(tick);
            tick += niceInterval;
        }

        return ticks.stream().mapToDouble(Double::doubleValue).toArray();
    }

    @Override
    public int getMaximumVerticalSize() {
        return MAXIMUM_HEIGHT;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return 2000;
    }

    @Override
    public int getMinimumVerticalSize() {
        return MINIMUM_HEIGHT;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return 100;
    }

    @Override
    public void draw(Subcanvas parent, Theme theme) {
        Subcanvas g = new Subcanvas(parent, parent.getWidth() - offset, parent.getHeight(), offset, 0);

        // Draw small tick marks between circles
        // Every other tick should be bigger
        // Continue ticks all the way to maxValue
        g.setPaint(theme.divider());
        if (circleValues.length > 1) {
            double interval = circleValues[1] - circleValues[0];
            drawTicks(g, interval);
            drawCircles(g, theme);
        }
    }

    private void drawCircles(Subcanvas g, Theme theme) {
        int height = g.getHeight();

        int y = height / 2;

        // Draw circles with labels at round numbers
        for (double value : circleValues) {
            // Calculate x position based on value
            int x = (int) (value * scaleGroup.getScale());

            // Drop the first circle
            if (value > 0) {
                // Draw circle background
                g.setPaint(theme.background());
                g.fillCircle(x, y, CIRCLE_DIAMETER);

                // Draw circle border
                g.setPaint(theme.divider());
                g.drawCircle(x, y, CIRCLE_DIAMETER);

                // Draw label centered in circle
                String labelText = formatter.format(value);
                Label label = new Label(labelText)
                        .setStyle(PLAIN)
                        .setTargetHeight(LABEL_FONT_SIZE)
                        .setHorizontalAlignment(Alignment.CENTER)
                        .setVerticalAlignment(VAlignment.MIDDLE);

                label.draw(g, x, y);
            }
        }


    }

    private void drawTicks(Subcanvas g, double interval) {
        double smallTickInterval = interval / NUM_TICKS;
        int height = g.getHeight();

        int y = height / 2;
        // Draw ticks from 0 to maxValue
        double currentValue = 0;
        int tickIndex = 0;
        while (currentValue <= maxValue) {
            int x = (int) (currentValue * scaleGroup.getScale());

            // Skip ticks that are within 2 pixels of any circle boundary
            if (! isNearCircleBoundary(x)) {
                int tickHeight = TICK_HEIGHT;
                int smallTickHeight = tickHeight / 2;

                // Every other tick should be taller
                if (tickIndex % 2 == 0) {
                    drawTick(g, x, y, tickHeight);
                } else {
                    drawTick(g, x, y, smallTickHeight);
                }
            }

            currentValue += smallTickInterval;
            tickIndex++;
        }
    }

    private boolean isNearCircleBoundary(int tickX) {
        // Check if tick is within 2 pixels of any circle boundary
        // Circle radius is CIRCLE_DIAMETER / 2, so boundary is at radius + 2 pixels from center
        int boundaryDistance = CIRCLE_DIAMETER / 2 + 2;

        for (double value : circleValues) {
            if (value > 0) { // Skip the first circle at 0
                int circleX = (int) (value * scaleGroup.getScale());
                if (Math.abs(tickX - circleX) <= boundaryDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void drawTick(Subcanvas g, int x, int y, int tickHeight) {
        int tickTop = y + tickHeight / 2;
        int tickBottom = y - tickHeight / 2;
        g.drawLine(x, tickTop, x, tickBottom, LINE_THICKNESS);
    }
}
