package io.quarkus.infra.performance.graphics.model;

public record BenchmarkData(
        Timing timing, Results results, Config config) {

    // For testing convenience – we may wish to push defaults further down the hierarchy
    public BenchmarkData() {
        this(null, null, null);
    }
}
