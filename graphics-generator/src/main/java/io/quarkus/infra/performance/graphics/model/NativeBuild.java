package io.quarkus.infra.performance.graphics.model;

import java.util.List;

public record NativeBuild(
        List<Double> rss,
        double binarySize
) {
}
