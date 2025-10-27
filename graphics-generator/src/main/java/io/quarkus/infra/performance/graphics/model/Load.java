package io.quarkus.infra.performance.graphics.model;

import java.util.List;

public record Load(
        List<Double> throughput,
        List<Double> rss,
        List<Double> throughputDensity,
        Double avThroughput,
        Double avMaxRss,
        Double maxThroughputDensity) {
}
