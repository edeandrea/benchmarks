package io.quarkus.infra.performance.graphics.model;

public record Result(
        Build build,
        Startup startup,
        Rss rss,
        Load load) {
}
