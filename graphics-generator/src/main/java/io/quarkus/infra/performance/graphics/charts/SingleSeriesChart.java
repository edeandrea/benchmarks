package io.quarkus.infra.performance.graphics.charts;

import java.util.List;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.SingleSeriesPlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

public abstract class SingleSeriesChart extends Chart {
    protected final List<Datapoint> data;
    protected final double maxValue;

    protected SingleSeriesChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        super(plotDefinition, bmData);
        if (plotDefinition instanceof SingleSeriesPlotDefinition singleSeriesPlotDefinition) {
            this.data = bmData.results().getDatasets(singleSeriesPlotDefinition.fun());
            maxValue = data.stream().map(d -> d.value().getValue()).max(Double::compare).orElse(1.0);
        } else {
            throw new IllegalArgumentException("Cannot construct a " + this.getClass().getName() + " with a " + plotDefinition.getClass());
        }
    }
}
