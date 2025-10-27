package io.quarkus.infra.performance.graphics.model;

import java.util.List;

public record Rss(
        List<Double> startup,
        List<Double> firstRequest,
        Double avStartupRss,
        Double avFirstRequestRss
) {
}

