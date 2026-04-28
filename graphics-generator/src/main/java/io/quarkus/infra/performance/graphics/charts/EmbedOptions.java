package io.quarkus.infra.performance.graphics.charts;

public record EmbedOptions(boolean isEmbedded, boolean isInverted, boolean showFrameworkLabels) {

    public static final EmbedOptions DEFAULT = new EmbedOptions(false, false, true);

    public EmbedOptions(boolean isInverted, boolean showFrameworkLabels) {
        this(true, isInverted, showFrameworkLabels);
    }
}
