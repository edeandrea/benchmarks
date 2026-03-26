package io.quarkus.infra.performance.graphics.charts;

/**
 * Abstract base class for elements that use a shared scale group.
 * Provides a final ScaleGroup field that subclasses can use for scaling operations.
 */
public abstract class ScaledElement implements ElasticElement {
    protected final ScaleGroup scaleGroup;
    protected int offset = 0;

    /**
     * Creates a scaled element with the specified ScaleGroup.
     *
     * @param scaleGroup the scale group to use for managing scale
     */
    protected ScaledElement(ScaleGroup scaleGroup) {
        this.scaleGroup = scaleGroup;
    }

    /**
     * Sets the horizontal offset for this element.
     *
     * @param offset the offset in pixels
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

}

