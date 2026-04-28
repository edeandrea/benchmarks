package io.quarkus.infra.performance.graphics.charts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;

public abstract class Chart implements ElasticElement {

    protected final Config metadata;
    // Adding elements into this collection includes them in size calculations. For size calculations, the assumption is that these are stacked vertically
    protected final Set<ElasticElement> children;
    protected final Title title;

    final int xmargins = 20;
    final int ymargins = 20;
    protected final Optional<EmbedOptions> embedOptions;
    int lxmargin = xmargins;
    int rxmargin = xmargins;

    protected Chart(PlotDefinition plotDefinition, BenchmarkData bmData, Optional<EmbedOptions> embedOptions) {
        this.metadata = bmData.config();
        this.embedOptions = embedOptions;

        children = new HashSet<>();

        this.title = new Title(plotDefinition.title(), plotDefinition.subtitle(), embedOptions);

        children.add(this.title);

    }

    public Chart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        this(plotDefinition, bmData, Optional.empty());
    }

    public void draw(Subcanvas g, Theme theme) {
        if (getMinimumHorizontalSize() > g.getWidth()) {
            throw new SizeException("Cannot fit " + getMinimumHorizontalSize() + "px of content into a " + g.getWidth()
                    + "px horizontal space.");
        }
        if (getMinimumVerticalSize() > g.getHeight()) {
            throw new SizeException("Cannot fit " + getMinimumVerticalSize() + "px of content into a " + g.getHeight()
                    + "px vertical space.");
        }

        int canvasHeight = g.getHeight();
        int canvasWidth = g.getWidth();

        // Don't fill background on embedded charts, because it's unnecessary, wastes xml space in the svg, and can overwrite other chart elements
        if (embedOptions.isEmpty()) {
            g.setPaint(theme.background());
            g.fill();
        }

        Subcanvas canvasWithMargins = new Subcanvas(g, canvasWidth - lxmargin - rxmargin, canvasHeight - 2 * ymargins, lxmargin,
                ymargins);

        drawNoCheck(canvasWithMargins, theme);
    }

    protected abstract void drawNoCheck(Subcanvas g, Theme theme);

    protected static void drawFinePrint(Subcanvas canvasWithMargins, Theme theme, int finePrintHeight, int yOffset, Optional<FinePrint> maybeFineprint) {

        if (maybeFineprint.isPresent()) {
            FinePrint fineprint = maybeFineprint.get();

            int finePrintWidth = Math.min(canvasWithMargins.getWidth(), fineprint.getActualHorizontalSize(finePrintHeight));
            int finePrintPadding = (canvasWithMargins.getWidth() - finePrintWidth) / 2;
            Subcanvas finePrintArea = new Subcanvas(canvasWithMargins, finePrintWidth, finePrintHeight, finePrintPadding, yOffset);
            fineprint.draw(finePrintArea, theme);
        }
    }

    // SVGs after to be done *after* the main drawing, because getting the document root before drawing causes all subsequent draws to be dropped.
    // This seems to be a characteristic of the Batik streaming model.
    public Collection<InlinedSVG> getInlinedSVGs() {
        return Collections.emptyList();
    }

    @Override
    public int getMaximumVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getMaximumVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getMaximumHorizontalSize).max().orElse(0) + lxmargin + rxmargin;
    }

    @Override
    public int getMinimumVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getMinimumVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getMinimumHorizontalSize).max().orElse(0) + lxmargin + rxmargin;
    }

    @Override
    public int getPreferredVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getPreferredVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getPreferredHorizontalSize).max().orElse(0) + lxmargin + rxmargin;
    }

    protected int getPreferredVerticalSize(int width) {
        return getPreferredVerticalSize();
    }

    /**
     * Subclasses can override if they should be aligned differently when embedded.
     */
    protected int getCenteringOffset() {
        return 0;
    }

    protected void suppressLeftMargin() {
        lxmargin = 0;
    }

    protected void suppressRightMargin() {
        rxmargin = 0;
    }
}
