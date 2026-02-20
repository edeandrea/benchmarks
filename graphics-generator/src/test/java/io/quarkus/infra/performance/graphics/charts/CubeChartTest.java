package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

public class CubeChartTest extends ChartTest {

    @Override
    protected CubeChart createChart(PlotDefinition plotDefinition, BenchmarkData data) {
        return new CubeChart(plotDefinition, data);
    }
}
