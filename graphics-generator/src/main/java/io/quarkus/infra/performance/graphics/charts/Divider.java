package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;

public class Divider implements ElasticElement {
    public static final int THICKNESS = 2;

    @Override
    public int getMaximumVerticalSize() {
        return 9 * THICKNESS;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return 1000;
    }

    @Override
    public int getMinimumVerticalSize() {
        return 3 * THICKNESS;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return 1;
    }

    @Override
    public void draw(Subcanvas g, Theme theme) {
        g.setPaint(theme.divider());
        int y = g.getHeight() / 2;
        g.drawLine(0, y, g.getWidth(), y, THICKNESS);
    }
}
