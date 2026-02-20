package io.quarkus.infra.performance.graphics.model;

import java.util.List;

import io.quarkus.infra.performance.graphics.PlotDefinition;

public record CompositePlotDefinition(String filename,
                                      List<PlotDefinition> pds) implements PlotDefinition {

    @Override
    public String title() {
        return "";
    }

    @Override
    public String subtitle() {
        return "";
    }
}
