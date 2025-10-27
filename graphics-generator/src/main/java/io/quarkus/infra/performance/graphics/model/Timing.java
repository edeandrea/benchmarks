package io.quarkus.infra.performance.graphics.model;

import java.time.Instant;

public record Timing(
        Instant start, Instant stop
) {
}
