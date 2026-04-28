package io.quarkus.infra.performance.graphics.charts;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.SingleSeriesPlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public abstract class SingleSeriesChart extends Chart {
    protected final List<Datapoint> data;
    protected final DimensionalNumber maxValue;

    protected SingleSeriesChart(PlotDefinition plotDefinition, BenchmarkData bmData, Optional<EmbedOptions> embedOptions) {
        super(plotDefinition, bmData, embedOptions);
        if (plotDefinition instanceof SingleSeriesPlotDefinition singleSeriesPlotDefinition) {
            this.data = bmData.results().getDatasets(singleSeriesPlotDefinition.fun());
            // Find max value, or create a default DimensionalNumber with value 1.0 if no data
            maxValue = data.stream()
                    .map(Datapoint::value)
                    .max(Comparator.comparingDouble(DimensionalNumber::getValue))
                    .orElseGet(() -> data.isEmpty() ? new EmptyDimensionalNumber(0):data.get(0).value());


        } else {
            throw new IllegalArgumentException("Cannot construct a " + this.getClass().getName() + " with a " + plotDefinition.getClass());
        }
    }

    // Fallback for the case where we try and construct a chart with no data, so no units
    class EmptyDimensionalNumber extends DimensionalNumber {
        public EmptyDimensionalNumber(double value) {
            super(value);
        }

        @Override
        public String getUnits() {
            return "";
        }
    }
}
