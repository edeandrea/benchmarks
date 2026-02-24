package io.quarkus.infra.performance.graphics;

import java.awt.Color;
import java.util.Map;

import io.quarkus.infra.performance.graphics.charts.fonts.EmbeddableFont;
import io.quarkus.infra.performance.graphics.model.Framework;

import static java.awt.Color.decode;

public record Theme(String name, Color background, Color text, Color divider, Color finePrint,
                    Map<Framework, Color> chartElements) {
    public static final EmbeddableFont FONT = EmbeddableFont.OPENSANS;
    public static final Color QUARKUS_BLUE = decode("#4695eb");
    public static final Color SPRING_GREEN = decode("#6ab443");
    public static final Color SPRING_LIGHT_GREEN = decode("#c3e1b4");

    public static final Color LIGHT_DIVIDER = decode("#AAAAAA");
    public static final Color DARK_DIVIDER = decode("#555555");

    public static final Color BARELY_GREY = decode("#F9FBFB");
    public static final Color OFF_BLACK = decode("#212121");

    private static final Map<Framework, Color> DEFAULT_CHART_ELEMENTS = Map.ofEntries(
            // Quarkus
            Map.entry(Framework.QUARKUS3_JVM, QUARKUS_BLUE),
            Map.entry(Framework.QUARKUS3_VIRTUAL, QUARKUS_BLUE),
            Map.entry(Framework.QUARKUS3_NATIVE, QUARKUS_BLUE),
            Map.entry(Framework.QUARKUS3_SPRING_COMPAT, QUARKUS_BLUE),

            // Spring 3
            Map.entry(Framework.SPRING3_JVM, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING3_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING3_JVM_AOT, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING3_VIRTUAL, SPRING_LIGHT_GREEN),

            // Spring 4
            Map.entry(Framework.SPRING4_JVM, SPRING_GREEN),
            Map.entry(Framework.SPRING4_NATIVE, SPRING_GREEN),
            Map.entry(Framework.SPRING4_JVM_AOT, SPRING_GREEN),
            Map.entry(Framework.SPRING4_VIRTUAL, SPRING_GREEN),

            // Spring
            Map.entry(Framework.SPRING_JVM, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING_JVM_AOT, SPRING_LIGHT_GREEN),
            Map.entry(Framework.SPRING_VIRTUAL, SPRING_LIGHT_GREEN));

    public static final Theme LIGHT = new Theme("light", Color.WHITE, decode("#121212"), LIGHT_DIVIDER, BARELY_GREY);
    public static final Theme DARK = new Theme("dark", decode("#121212"), decode("#B5B5B5"), DARK_DIVIDER, OFF_BLACK);

    public Theme(String name, Color background, Color text, Color divider, Color finePrint) {
        this(name, background, text, divider, finePrint, DEFAULT_CHART_ELEMENTS);
    }

}
