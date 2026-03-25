package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.model.units.Memory;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CubesTest {

    @Test
    public void testBoundsOnDimensionsForFrameworkWithShortNameAndHighValue() {
        Memory m = new Memory(315);
        Datapoint d = new Datapoint(SPRING3_JVM, m);

        Cubes chart = new Cubes(d, new CubeGroup());

        int preferredWidth = chart.getPreferredHorizontalSize();
        int preferredHeight = chart.getPreferredVerticalSize();

        int minimumWidth = chart.getMinimumHorizontalSize();
        int minimumHeight = chart.getMinimumVerticalSize();

        int maximumWidth = chart.getMaximumHorizontalSize();
        int maximumHeight = chart.getMaximumVerticalSize();

        assertTrue(preferredWidth <= maximumWidth, preferredWidth + " > " + maximumWidth);
        assertTrue(preferredWidth >= minimumWidth, preferredWidth + " > " + minimumWidth);

        assertTrue(preferredHeight <= maximumHeight, preferredHeight + " > " + maximumHeight);
        assertTrue(preferredHeight >= minimumHeight, preferredHeight + " > " + minimumHeight);
    }

    @Test
    public void testBoundsOnDimensionsForFrameworkWithLongNameAndLowValue() {
        Memory m = new Memory(70);
        Datapoint d = new Datapoint(QUARKUS3_VIRTUAL, m);

        Cubes chart = new Cubes(d, new CubeGroup());

        int preferredWidth = chart.getPreferredHorizontalSize();
        int preferredHeight = chart.getPreferredVerticalSize();

        int minimumWidth = chart.getMinimumHorizontalSize();
        int minimumHeight = chart.getMinimumVerticalSize();

        int maximumWidth = chart.getMaximumHorizontalSize();
        int maximumHeight = chart.getMaximumVerticalSize();

        assertTrue(preferredWidth <= maximumWidth, preferredWidth + " > " + maximumWidth);
        assertTrue(preferredWidth >= minimumWidth, preferredWidth + " > " + minimumWidth);

        assertTrue(preferredHeight <= maximumHeight, preferredHeight + " > " + maximumHeight);
        assertTrue(preferredHeight >= minimumHeight, preferredHeight + " > " + minimumHeight);
    }

}
