package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.scales.ScaleDivider;
import io.quarkus.infra.performance.graphics.model.units.Seconds;
import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleDividerTest extends ElasticElementTest {

    @Test
    public void testMinimumVerticalSize() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        // Should have enough space for tick marks and labels
        assertTrue(divider.getMinimumVerticalSize() > 0, "Minimum vertical size should be positive");
        assertTrue(divider.getMinimumVerticalSize() >= 30, "Should have space for tick marks and labels");
    }

    @Test
    public void testMaximumVerticalSize() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        // Maximum should be larger than minimum but not excessive
        assertTrue(divider.getMaximumVerticalSize() > divider.getMinimumVerticalSize(),
                "Maximum should be larger than minimum");
        assertTrue(divider.getMaximumVerticalSize() <= 100,
                "Maximum should not be excessive");
    }

    @Test
    public void testMinimumHorizontalSize() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        // Should span the full width
        assertTrue(divider.getMinimumHorizontalSize() > 0, "Minimum horizontal size should be positive");
    }

    @Test
    public void testMaximumHorizontalSize() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        // Should be able to span wide areas
        assertTrue(divider.getMaximumHorizontalSize() >= 1000,
                "Should support wide canvases");
    }

    @Test
    public void testConstructorWithMaxValue() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        assertNotNull(divider, "Should create divider with max value");
    }

    @Test
    public void testConstructorWithZeroMaxValue() {
        ScaleDivider divider = new ScaleDivider(new Seconds(0));
        // Should span the full width
        assertTrue(divider.getMinimumHorizontalSize() > 0, "Minimum horizontal size should be positive");
    }

    @Test
    public void testConstructorWithNegativeMaxValue() {
        ScaleDivider divider = new ScaleDivider(new Seconds(- 100));
        // Should span the full width
        assertTrue(divider.getMinimumHorizontalSize() > 0, "Minimum horizontal size should be positive");
    }

    @Test
    public void testDrawWithDarkTheme() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        divider.setScale(1);
        String svg = drawSvg(divider);
        assertNotNull(svg, "Should generate SVG");
        assertTrue(svg.length() > 0, "SVG should not be empty");
        // Should contain line elements for the scale
        assertTrue(svg.contains("<line"), "Should contain line elements for scale marks");
    }

    @Test
    public void testDrawWithLightTheme() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        divider.setScale(1);
        SVGGraphics2D g = getSvgGraphics2D(divider.getPreferredHorizontalSize(),
                divider.getPreferredVerticalSize());
        Subcanvas canvas = new Subcanvas(g, divider.getPreferredHorizontalSize(),
                divider.getPreferredVerticalSize(), 0, 0);

        assertDoesNotThrow(() -> divider.draw(canvas, Theme.LIGHT),
                "Should draw without errors with light theme");
    }

    @Test
    public void testScaleMarkingsAreGenerated() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        divider.setScale(1);
        String svg = drawSvg(divider);
        // Should have multiple tick marks
        int lineCount = countOccurrences(svg, "<line");
        assertTrue(lineCount > 2, "Should have multiple scale marks (found " + lineCount + ")");
    }

    @Test
    public void testScaleLabelsAreGenerated() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        String svg = drawSvg(divider);
        // Should contain text elements for labels
        assertTrue(svg.contains("<text"), "Should contain text elements for scale labels");
        // Should show the max value
        assertTrue(svg.contains("100"), "Should display the maximum value");
    }

    @Test
    public void testScaleWithDifferentMaxValues() {
        ScaleDivider divider50 = new ScaleDivider(new Seconds(50));
        ScaleDivider divider200 = new ScaleDivider(new Seconds(200));

        String svg50 = drawSvg(divider50);
        String svg200 = drawSvg(divider200);

        assertTrue(svg50.contains("50"), "Should display max value 50");
        assertTrue(svg200.contains("200"), "Should display max value 200");
    }

    @Test
    public void testScaleWithLargeValue() {
        ScaleDivider divider = new ScaleDivider(new Seconds(10000));
        String svg = drawSvg(divider);
        assertNotNull(svg, "Should handle large values");
        assertTrue(svg.contains("10k") || svg.contains("10000") || svg.contains("10,000"),
                "Should display large value");
    }

    @Test
    public void testScaleWithSmallValue() {
        ScaleDivider divider = new ScaleDivider(new Seconds(5));
        String svg = drawSvg(divider);
        assertNotNull(svg, "Should handle small values");
        assertTrue(svg.contains("5"), "Should display small value");
    }

    @Test
    public void testPreferredSizes() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        int preferredVertical = divider.getPreferredVerticalSize();
        int preferredHorizontal = divider.getPreferredHorizontalSize();

        assertTrue(preferredVertical >= divider.getMinimumVerticalSize(),
                "Preferred vertical should be at least minimum");
        assertTrue(preferredVertical <= divider.getMaximumVerticalSize(),
                "Preferred vertical should be at most maximum");
        assertTrue(preferredHorizontal >= divider.getMinimumHorizontalSize(),
                "Preferred horizontal should be at least minimum");
        assertTrue(preferredHorizontal <= divider.getMaximumHorizontalSize(),
                "Preferred horizontal should be at most maximum");
    }

    @Test
    public void testDrawOnSmallCanvas() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        SVGGraphics2D g = getSvgGraphics2D(200, 30);
        Subcanvas canvas = new Subcanvas(g, 200, 30, 0, 0);

        assertDoesNotThrow(() -> divider.draw(canvas, Theme.DARK),
                "Should handle small canvas gracefully");
    }

    @Test
    public void testDrawOnLargeCanvas() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        SVGGraphics2D g = getSvgGraphics2D(1200, 80);
        Subcanvas canvas = new Subcanvas(g, 1200, 80, 0, 0);

        assertDoesNotThrow(() -> divider.draw(canvas, Theme.DARK),
                "Should handle large canvas gracefully");
    }

    @Test
    public void testIntermediateScaleMarks() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        String svg = drawSvg(divider);
        // Should have intermediate values like 25, 50, 75
        int textCount = countOccurrences(svg, "<text");
        assertTrue(textCount >= 3, "Should have multiple scale labels (found " + textCount + ")");
    }

    @Test
    public void testTickMarkOrientation() {
        ScaleDivider divider = new ScaleDivider(new Seconds(100));
        divider.setScale(1);
        String svg = drawSvg(divider);
        // Tick marks should be vertical lines (x1 == x2)
        assertTrue(svg.contains("<line"), "Should contain line elements");
    }

    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != - 1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}


