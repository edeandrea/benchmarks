package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Cgroup(
        @JsonProperty("mem_max")
        String memMax,
        String name,
        String cpu) {
}