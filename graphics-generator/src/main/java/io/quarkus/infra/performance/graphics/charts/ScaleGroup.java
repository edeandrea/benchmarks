package io.quarkus.infra.performance.graphics.charts;

/**
 * A group that manages a shared scale value for multiple elements (Bars and ScaleDividers).
 * Elements hold a reference to this group and retrieve the scale when needed.
 * This avoids the need to call setScale() on each element individually.
 */
public class ScaleGroup {
    private double scale = 1.0;

    public ScaleGroup() {
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}

// Made with Bob
