package io.quarkus.infra.performance.graphics.charts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

import static io.quarkus.infra.performance.graphics.charts.Cubes.MAXIMUM_CUBE_SIZE;
import static java.util.Collections.emptyList;

public class CubeChart extends SingleSeriesChart {

    //    The ideal ratio between space for cubes and space for labels
    private static final int TARGET_CUBE_LABEL_RATIO = 4;
    private static final int MINIMUM_GUTTER = 14;

    // These graphs can go quite wide, and we want to cap that on the preferred size
    private static final int MAXIMUM_NATURAL_WIDTH = 2048;
    private final Optional<FinePrint> fineprint;
    private final List<Cubes> cubes = new ArrayList<>();
    private final Optional<Cubes> tallestCube;
    private final CubeGroup cubeGroup;

    public CubeChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        this(plotDefinition, bmData, false);

    }

    public CubeChart(PlotDefinition plotDefinition, BenchmarkData bmData, boolean isEmbedded) {
        super(plotDefinition, bmData);

        if (! isEmbedded) {
            this.fineprint = Optional.of(new FinePrint(bmData));
            children.add(fineprint.get());
        } else {
            fineprint = Optional.empty();
        }

        cubeGroup = new CubeGroup();
        cubeGroup.setNumCubesPerColumn(16);

        for (Datapoint d : data) {
            Cubes c = new Cubes(d, cubeGroup);
            cubes.add(c);

        }

        // Add the tallest cube to the children, for height calculations
        tallestCube = cubes.stream()
                .max(Comparator.comparing(Cubes::getPreferredVerticalSize));

        tallestCube.ifPresent(children::add);
    }

    @Override
    public int getMaximumHorizontalSize() {
        return Math.max(Math.max(fineprint.map(ElasticElement::getMaximumHorizontalSize).orElse(0), cubes.stream().mapToInt(ElasticElement::getMaximumHorizontalSize).sum()), title.getMaximumHorizontalSize()) + 2 * xmargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return Math.max(Math.max(fineprint.map(ElasticElement::getMinimumHorizontalSize).orElse(0), cubes.stream().mapToInt(ElasticElement::getMinimumHorizontalSize).sum()), title.getMinimumHorizontalSize()) + 2 * xmargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        int naturalWidth = Math.max(Math.max(fineprint.map(ElasticElement::getPreferredHorizontalSize).orElse(0), cubes.stream().mapToInt(ElasticElement::getPreferredHorizontalSize).sum()), title.getPreferredHorizontalSize()) + 2 * xmargins;
        if (naturalWidth > MAXIMUM_NATURAL_WIDTH) {
            return Math.max(getMinimumHorizontalSize(), MAXIMUM_NATURAL_WIDTH);
        } else {
            return naturalWidth;
        }
    }

    @Override
    public int getPreferredVerticalSize() {
        // The vertical size and horizontal size are coupled, so base the vertical size on the horizontal size
        return getPreferredVerticalSize(getPreferredHorizontalSize());
    }

    @Override
    public int getPreferredVerticalSize(int horizontalSize) {
        int defaultPreferred = super.getPreferredVerticalSize();
        if (tallestCube.isPresent()) {
            // Calibrate the cube sizes to the given width, assuming a big (but not infinite) height (the height parameter shouldn't matter except in rare cases)
            workOutCubeSizes(horizontalSize, 4 * horizontalSize);

            return defaultPreferred - tallestCube.get().getPreferredVerticalSize() + tallestCube.get().getActualVerticalSize();
        } else {
            return defaultPreferred;
        }
    }


    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {

        canvasWithMargins.setPaint(theme.text());

        // Start by assuming we can have the preferred size for title and fine print
        int finePrintHeight = fineprint.isPresent() ? fineprint.get().getPreferredVerticalSize():0;
        int titleHeight = title.getPreferredVerticalSize();

        // ... but then check if that actually fits
        int plotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;

        int minimumCubesSize = tallestCube.map(Cubes::getMinimumVerticalSize).orElse(0);

        // If it doesn't fit, shrink the fine print and title so the actual plot has the minimum it needs
        if (plotHeight < minimumCubesSize) {
            int delta = minimumCubesSize - plotHeight;
            if (fineprint.isPresent()) {
                finePrintHeight -= delta / 2;
                titleHeight -= delta / 2;
            } else {
                titleHeight -= delta;
            }
            plotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;
        }

        // Ok, hopefully we're good and can start drawing

        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(), titleHeight,
                0, 0);

        int x = 0;

        title.draw(titleCanvas, theme);

        Subcanvas plotArea = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(),
                plotHeight, 0, titleCanvas.getHeight());

        // Now check in the cube bits themselves to adjust cube sizes and font sizes
        int gutterSize = workOutCubeSizes(plotArea);

        for (Cubes c : cubes) {
            int width = c.getActualHorizontalSize();
            Subcanvas dataArea = new Subcanvas(plotArea, width, plotArea.getHeight(), x, 0);

            c.draw(dataArea, theme);
            x += dataArea.getWidth() + gutterSize;
        }

        drawFinePrint(canvasWithMargins, theme, finePrintHeight, titleCanvas.getHeight() + plotArea.getHeight(), fineprint);
    }


    private int workOutCubeSizes(Subcanvas plotArea) {
        return workOutCubeSizes(plotArea.getWidth(), plotArea.getHeight());
    }

    private int workOutCubeSizes(int targetWidth, int targetHeight) {

        // Here's the algorithm we follow to try and fit everything in, when everything affects everything else.
        // 1. Does the text fit in horizontally? If not, shrink it until it fits – we can do this first.
        // 2. Shrink the cubes until all the cubes fits in horizontally
        // 3. Now check if things fit in vertically? If not, we have a choice between shrinking cubes, or shrinking text. That’s a decision we make based on how much vertical space each occupies.


        int numGutters = data.size() - 1;
        int gutterPadding = MINIMUM_GUTTER * numGutters;

        // Step 1 - make sure all the labels fit horizontally
        int totalTextWidth = cubes.stream().mapToInt(Cubes::getTextWidth).sum();

        int availableWidth = targetWidth - gutterPadding;

        while (totalTextWidth > availableWidth) {
            decrementFonts();
            totalTextWidth = cubes.stream().mapToInt(Cubes::getTextWidth).sum();
        }

        // Now make sure all the cubes fit horizontally
        cubeGroup.setTotalCubeSize(MAXIMUM_CUBE_SIZE);
        int totalWidth = cubes.stream().mapToInt(Cubes::getActualHorizontalSize).sum();

        while (totalWidth > availableWidth) {
            cubeGroup.decrementTotalCubeSize();
            totalWidth = cubes.stream().mapToInt(Cubes::getActualHorizontalSize).sum();
        }

        if (tallestCube.isPresent()) {
            // Almost there, but things might still not fit vertically
            int height = tallestCube.map(Cubes::getActualVerticalSize).orElse(0);

            // If it's too tall, decide whether to shrink the fonts or the cubes to get the best-looking result
            while (height > targetHeight) {
                int cubesHeight = tallestCube.get().getActualCubesHeight();
                int labelHeight = tallestCube.get().getActualLabelHeight();

                if ((cubesHeight / labelHeight) > TARGET_CUBE_LABEL_RATIO) {
                    cubeGroup.decrementTotalCubeSize();
                } else {
                    decrementFonts();
                }
                height = tallestCube.map(Cubes::getActualVerticalSize).orElse(0);
            }
        }

        int actualOccupiedArea = 0;

        // Work out how much space is used, so we can space the sections evenly
        for (Cubes c : cubes) {
            int width = c.getActualHorizontalSize();
            actualOccupiedArea += width;
        }

        int actualGutterPadding = numGutters > 0 ? (targetWidth - actualOccupiedArea) / numGutters:0;

        return actualGutterPadding;
    }

    private void decrementFonts() {
        // If it doesn't fit, shrink fonts
        for (Cubes c : cubes) {
            c.decrementFonts();
        }
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        if (fineprint.isPresent()) {
            return fineprint.get().getInlinedSVGs();
        } else {
            return emptyList();
        }
    }

}
