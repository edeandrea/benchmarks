package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

public class BarChartTest extends ChartTest {

    @Override
    protected BarChart createChart(PlotDefinition plotDefinition, BenchmarkData data) {
        return new BarChart(plotDefinition, data);
    }


}