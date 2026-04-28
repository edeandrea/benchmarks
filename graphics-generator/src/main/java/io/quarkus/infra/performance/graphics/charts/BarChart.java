package io.quarkus.infra.performance.graphics.charts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.scales.ScaleDivider;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Category;

import static java.util.Collections.emptyList;

public class BarChart extends SingleSeriesChart {

    private static final int MINIMUM_PARTITION_SIZE = 2;
    private final Optional<FinePrint> fineprint;
    private final List<Bar> bars = new ArrayList<>();
    private final List<ScaledElement> barsAndPartitions = new ArrayList<>();
    private final LabelGroup frameworkLabelGroup = new LabelGroup(Bar.LEFT_LABEL_SIZE);
    private final LabelGroup valueLabelGroup = new LabelGroup();
    private final ScaleGroup scaleGroup = new ScaleGroup();

    public BarChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        this(plotDefinition, bmData, Optional.empty());
    }

    public BarChart(PlotDefinition plotDefinition, BenchmarkData bmData, Optional<EmbedOptions> embedOptions) {
        super(plotDefinition, bmData, embedOptions);

        boolean isInverted = embedOptions.isPresent() ? embedOptions.get().isInverted():false;
        boolean isEmbedded = embedOptions.isPresent();

        if (! isEmbedded) {
            this.fineprint = Optional.of(new FinePrint(bmData));
            children.add(fineprint.get());
        } else {
            fineprint = Optional.empty();
        }

        Category previousCategory = null;
        int partitionCount = countPartitions(data) + 1;
        int averagePartitionSize = data.size() / partitionCount;
        // Do an extra check, so we don't get stupidly small partitions; technically maybe we'd prefer to check the ratio
        boolean shouldUsePartitions = (averagePartitionSize >= MINIMUM_PARTITION_SIZE);

        for (Datapoint d : data) {
            Category newCategory = d.framework().getPartitionableCategory();
            if (shouldUsePartitions) {
                if (newCategory != null && previousCategory != null && ! newCategory.equals(previousCategory)) {
                    ScaleDivider e1 = new ScaleDivider(maxValue, scaleGroup, isInverted);
                    barsAndPartitions.add(e1);
                    children.add(e1);
                }
            }

            Bar e = new Bar(d, frameworkLabelGroup, valueLabelGroup, scaleGroup, embedOptions);
            bars.add(e);
            barsAndPartitions.add(e);
            children.add(e);
            previousCategory = newCategory;
        }

    }

    public BarChart(PlotDefinition pd, BenchmarkData bmData, EmbedOptions embedOptions) {
        this(pd, bmData, Optional.of(embedOptions));
    }

    private int countPartitions(List<Datapoint> data) {
        Category previousCategory = null;
        int partitions = 0;
        for (Datapoint d : data) {
            Category newCategory = d.framework().getPartitionableCategory();
            if (newCategory != null && previousCategory != null && ! newCategory.equals(previousCategory)) {
                partitions++;
            }
            previousCategory = newCategory;
        }
        return partitions;
    }

    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {
        int finePrintHeight = fineprint.isPresent() ? fineprint.get().getPreferredVerticalSize():0;
        int titleHeight = title.getPreferredVerticalSize();


        // ... but then check if that actually fits
        int barHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;

        int requiredBarHeight = barsAndPartitions.stream().mapToInt(ElasticElement::getMinimumVerticalSize).sum();

        // If it doesn't fit, shrink the fine print and title so the actual plot has the minimum it needs
        if (barHeight < requiredBarHeight) {
            int delta = requiredBarHeight - barHeight;
            if (fineprint.isPresent()) {
                finePrintHeight -= delta / 2;
                titleHeight -= delta / 2;
            } else {
                titleHeight -= delta;
            }
            barHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;
        }

        int leftLabelWidth = getLeftLabelWidth();

        // Set a common offset for all bars and partitions before trying to calculate a scale
        int offset = leftLabelWidth + Bar.LABEL_PADDING;
        for (ScaledElement element : barsAndPartitions) {
            element.setOffset(offset);
        }

        canvasWithMargins.setPaint(theme.text());
        int titleOffset = embedOptions.isPresent() && ! embedOptions.get().isInverted() ? offset:0;
        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(), titleHeight,
                titleOffset, 0);
        title.draw(titleCanvas, theme);

        Subcanvas barArea = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(),
                barHeight, 0, titleCanvas.getHeight());

        double scale = bars.stream().mapToDouble(bar -> bar.getMaximumScale(barArea)).min().orElse(1);

        int y = 0;

        canvasWithMargins.setPaint(theme.text());

        scaleGroup.setScale(scale);

        int extraVerticalSpace = barArea.getHeight() - barsAndPartitions.stream().mapToInt(ElasticElement::getMinimumVerticalSize).sum();
        int padding = barsAndPartitions.isEmpty() ? 0:extraVerticalSpace / barsAndPartitions.size();

        for (ScaledElement bar : barsAndPartitions) {
            Subcanvas individualBarArea = new Subcanvas(barArea, barArea.getWidth(), bar.getMinimumVerticalSize() + padding, 0, y);

            bar.draw(individualBarArea, theme);
            y += individualBarArea.getHeight();

        }

        if (embedOptions.isPresent()) {
            barArea.setPaint(theme.divider());
            int x = embedOptions.get().isInverted() ? barArea.getWidth() - offset:offset;
            barArea.drawLine(x, 0, x, barArea.getHeight());
        }

        drawFinePrint(canvasWithMargins, theme, finePrintHeight, barArea.getHeight() + titleCanvas.getHeight(), fineprint);
    }

    private int getLeftLabelWidth() {
        return Sizer.calculateWidth(bars.stream().map(Bar::getLeftLabelText).collect(Collectors.toSet()),
                frameworkLabelGroup.getFontSize());
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        if (fineprint.isPresent()) {
            return fineprint.get().getInlinedSVGs();
        } else {
            return emptyList();
        }
    }

    @Override
    public int getCenteringOffset() {
        if (embedOptions.isPresent() && embedOptions.get().showFrameworkLabels()) {
            return - 1 * getLeftLabelWidth() / 2;
        } else {
            return super.getCenteringOffset();
        }
    }
}
