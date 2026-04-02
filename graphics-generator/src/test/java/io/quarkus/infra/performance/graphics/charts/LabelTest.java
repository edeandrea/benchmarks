package io.quarkus.infra.performance.graphics.charts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelTest extends ElasticElementTest {
    @Test
    public void setTargetHeightInSingleLineCase() {
        Label label = new Label("First");
        int target = 40;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }

    @Test
    public void setTargetHeightInMultilineCase() {
        Label label = new Label("First\nSecond\nThird");
        // This value is chosen so that rounding errors don't matter
        int target = 42;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }

    @Test
    public void setTargetHeightInMultilineCaseWithLineSpacing() {
        Label label = new Label("First\nSecond\nThird");
        label.setLineSpacing(3);
        // A value for which rounding errors don't come into play
        int target = 81;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }
}
