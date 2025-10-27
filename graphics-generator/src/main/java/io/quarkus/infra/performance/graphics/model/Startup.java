package io.quarkus.infra.performance.graphics.model;

import java.util.List;

public record Startup(
        List<Double> timings,
        Double avStartTime
) {
}