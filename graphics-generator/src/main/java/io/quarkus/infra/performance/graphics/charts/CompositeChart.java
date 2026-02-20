package io.quarkus.infra.performance.graphics.charts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.CompositePlotDefinition;

public class CompositeChart extends Chart {

    private static final int DIVIDER_WIDTH = 1;

    private final FinePrint fineprint;
    private List<Chart> charts = new ArrayList<>();

    public CompositeChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        super(plotDefinition, bmData);
        // The layout is a bit coupled to the chart types, so although arbitrary numbers of definitions can get passed in, only three got plotted, and they look best if they're a certain set
        if (plotDefinition instanceof CompositePlotDefinition compositePlotDefinition) {
            for (PlotDefinition pd : compositePlotDefinition.pds()) {
                Chart chart;
                if (pd.title().contains("Memory")) {
                    chart = new CubeChart(pd, bmData, true);
                } else {
                    chart = new BarChart(pd, bmData, true);
                }

                charts.add(chart);
            }

            // Only some of the charts contribute to the height because we stack the first two side-by-side
            if (! charts.isEmpty()) {
                children.add(charts.getFirst());
                if (charts.size() > 1) {
                    children.add(charts.getLast());
                }
            }

            this.fineprint = new FinePrint(bmData);
            children.add(fineprint);
        } else {
            throw new IllegalArgumentException("Cannot construct a " + this.getClass().getName() + " with a " + plotDefinition.getClass());
        }
        // No title on the composite plots
        children.remove(title);
    }

    @Override
    public int getPreferredHorizontalSize() {
        // Scale up a bit from the minimum, but only a bit
        return (int) (getMinimumHorizontalSize() * 1.25);
    }

    @Override
    public int getMinimumVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getMinimumVerticalSize).sum() + 2 * ymargins + DIVIDER_WIDTH;
    }

    @Override
    public int getMinimumHorizontalSize() {
        int barChartsWidth = charts.getFirst().getMinimumHorizontalSize() + charts.get(1).getMinimumHorizontalSize();
        int cubeChartWidth = charts.getLast().getMinimumHorizontalSize();
        return Math.max(barChartsWidth, cubeChartWidth) + 2 * xmargins + DIVIDER_WIDTH;
    }

    @Override
    public int getPreferredVerticalSize() {
        // The vertical size and horizontal size are coupled for cube charts, so base the vertical size on the horizontal size
        return getPreferredVerticalSize(getPreferredHorizontalSize());
    }

    @Override
    public int getPreferredVerticalSize(int horizontalSize) {
        int childHeights = 0;
        for (ElasticElement child : children) {
            if (child instanceof Chart chart) {
                childHeights += chart.getPreferredVerticalSize(horizontalSize);
            } else {
                childHeights += child.getPreferredVerticalSize();
            }
        }

        return childHeights + 2 * ymargins + DIVIDER_WIDTH;

    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        return fineprint.getInlinedSVGs();
    }

    @Override
    protected void drawNoCheck(Subcanvas g, Theme theme) {

        int finePrintHeight = fineprint.getPreferredVerticalSize();
        // Make sure the fine print fits in width-wise

        while (fineprint.getActualHorizontalSize(finePrintHeight) > g.getWidth()) {
            finePrintHeight -= 10;
        }

        Chart bottomChart = charts.getLast();
        int bottomChartsHeight = Math.min(g.getHeight() / 2, bottomChart.getPreferredVerticalSize(g.getWidth()));
        int topChartsHeight = g.getHeight() - bottomChartsHeight - finePrintHeight;

        // If we didn't have enough room, take some back from the fine print and cube chart
        Chart leftChart = charts.getFirst();
        if (topChartsHeight < leftChart.getMinimumVerticalSize()) {
            int delta = leftChart.getMinimumVerticalSize() - topChartsHeight;
            topChartsHeight = leftChart.getMinimumVerticalSize();
            bottomChartsHeight = Math.max(bottomChartsHeight - delta / 2, bottomChart.getMinimumVerticalSize());
            finePrintHeight = g.getHeight() - topChartsHeight - bottomChartsHeight - DIVIDER_WIDTH;
        }

        Subcanvas topChartArea = new Subcanvas(g, g.getWidth(), topChartsHeight, 0, 0);

        Subcanvas leftArea = new Subcanvas(topChartArea, topChartArea.getWidth() / 2, topChartArea.getHeight(), 0, 0);
        leftChart.draw(leftArea, theme);

        Subcanvas rightArea = new Subcanvas(topChartArea, topChartArea.getWidth() / 2, topChartArea.getHeight(), leftArea.getWidth(), 0);
        Chart rightChart = charts.get(1);
        rightChart.draw(rightArea, theme);

        topChartArea.setPaint(theme.divider());
        topChartArea.fillRect(leftArea.getWidth(), 0, DIVIDER_WIDTH, topChartArea.getHeight());


        Subcanvas bottomChartArea = new Subcanvas(g, g.getWidth(), bottomChartsHeight, 0, topChartArea.getHeight() + + DIVIDER_WIDTH);
        bottomChart.draw(bottomChartArea, theme);

        topChartArea.setPaint(theme.divider());
        topChartArea.fillRect(0, topChartArea.getHeight() - DIVIDER_WIDTH, topChartArea.getWidth(), DIVIDER_WIDTH);


        int finePrintWidth = Math.min(g.getWidth(), fineprint.getActualHorizontalSize(finePrintHeight));
        int finePrintPadding = (g.getWidth() - finePrintWidth) / 2;
        Subcanvas finePrintArea = new Subcanvas(g, finePrintWidth, finePrintHeight, finePrintPadding, g.getHeight() - finePrintHeight);

        fineprint.draw(finePrintArea, theme);
    }
}
