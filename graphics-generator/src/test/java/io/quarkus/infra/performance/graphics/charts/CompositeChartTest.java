package io.quarkus.infra.performance.graphics.charts;

import java.util.List;
import java.util.function.Function;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.SingleSeriesPlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.CompositePlotDefinition;
import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public class CompositeChartTest extends ChartTest {

    @Override
    protected CompositeChart createChart(PlotDefinition plotDefinition, BenchmarkData data) {
        return new CompositeChart(plotDefinition, data);
    }

    @Override
    protected PlotDefinition createPlotDefinition() {
        Function<Result, ? extends DimensionalNumber> fun = framework -> framework.load().avThroughput();
        PlotDefinition subplot = new SingleSeriesPlotDefinition("test plot", "some subtitle", fun);
        PlotDefinition plotDefinition = new CompositePlotDefinition("test plot", List.of(subplot, subplot, subplot));
        return plotDefinition;
    }

}