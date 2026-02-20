package io.quarkus.infra.performance.graphics.charts;

import java.awt.Font;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.Bar.BAR_THICKNESS;

public class Cubes implements ElasticElement {
    private static final int LABEL_HEIGHT = BAR_THICKNESS;
    public static final double DECREMENT_INCREMENT = 0.97;
    private final Datapoint d;
    private static final int CUBE_PADDING = 1;
    private final Label valueLabel;
    private final Label frameworkLabel;
    private static final int MINIMUM_CUBE_SIZE = 2;
    private static final int MINIMUM_FONT_SIZE = 8;
    private static final int MAXIMUM_FONT_SIZE = 24;
    static final int MAXIMUM_CUBE_SIZE = 20;
    private final CubeGroup cubeGroup;

    public Cubes(Datapoint d, CubeGroup cubeGroup) {
        this.d = d;
        this.cubeGroup = cubeGroup;
        double val = d.value().getValue();

        frameworkLabel = new Label(d.framework().getExpandedName())
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.TOP)
                .setStyles(new int[]{Font.BOLD, Font.PLAIN})
                .setTargetHeight(LABEL_HEIGHT);
        valueLabel = new Label(String.format("%d %s", Math.round(val), d.value().getUnits()))
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.TOP)
                .setStyle(Font.BOLD).setTargetHeight(LABEL_HEIGHT * 2 / 3);
    }

    @Override
    public int getMaximumVerticalSize() {
        return (int) Math.min(cubeGroup.getNumCubesPerColumn(), d.value().getValue()) * MAXIMUM_CUBE_SIZE + valueLabel.getTargetHeight()
                + frameworkLabel.getTargetHeight();
    }

    @Override
    public int getMaximumHorizontalSize() {
        int cubesWidth = (int) Math.ceil(d.value().getValue() / cubeGroup.getNumCubesPerColumn() * MAXIMUM_CUBE_SIZE);
        double widestLabel = Math.max(valueLabel.calculateWidth(MAXIMUM_FONT_SIZE), frameworkLabel.calculateWidth(MAXIMUM_FONT_SIZE));
        return (int) Math.max(widestLabel, cubesWidth);
    }

    @Override
    public int getMinimumVerticalSize() {
        return (int) Math.min(cubeGroup.getNumCubesPerColumn(), d.value().getValue()/cubeGroup.getUnitsPerCube()) * MINIMUM_CUBE_SIZE + valueLabel.getTargetHeight()
                + frameworkLabel.getTargetHeight();
    }

    @Override
    public int getMinimumHorizontalSize() {

        double widestLabel = Math.max(valueLabel.calculateWidth(MINIMUM_FONT_SIZE), frameworkLabel.calculateWidth(MINIMUM_FONT_SIZE));
        int cubesWidth = (int) Math.ceil(d.value().getValue() / cubeGroup.getNumCubesPerColumn() * MINIMUM_CUBE_SIZE);
        return (int) Math.max(widestLabel, cubesWidth);
    }


    public int getTextWidth() {
        return Math.max(valueLabel.calculateWidth(), frameworkLabel.calculateWidth());
    }

    public int getActualHorizontalSize() {
        int widestLabel = getTextWidth();
        int cubesWidth = (int) Math.ceil(d.value().getValue() / cubeGroup.getNumCubesPerColumn() * cubeGroup.getTotalCubeSize());
        return Math.max(widestLabel, cubesWidth);
    }


    public int getActualVerticalSize() {
        return getActualLabelHeight() + getActualCubesHeight();
    }

    int getActualCubesHeight() {
        return (int) Math.min(Math.round(d.value().getValue() / cubeGroup.getUnitsPerCube()), cubeGroup.getNumCubesPerColumn()) * cubeGroup.getTotalCubeSize();
    }

    public int getActualLabelHeight() {
        return valueLabel.getActualHeight() + frameworkLabel.getActualHeight();
    }


    @Override
    public void draw(Subcanvas dataArea, Theme theme) {
        double val = d.value().getValue();
        // If this framework isn't found, it will just be the text colour, which is fine
        dataArea.setPaint(theme.chartElements().get(d.framework()));

        int cubeSize = cubeGroup.getTotalCubeSize() - CUBE_PADDING;

        Subcanvas cubeArea = new Subcanvas(dataArea, dataArea.getWidth(), cubeGroup.getNumCubesPerColumn() * cubeGroup.getTotalCubeSize(), 0, 0);

        Subcanvas labelArea = new Subcanvas(dataArea, dataArea.getWidth(), dataArea.getHeight() - cubeArea.getHeight(), 0,
                cubeArea.getHeight());

        int startingY = cubeArea.getHeight() - cubeGroup.getTotalCubeSize();

        // Center the cubes
        int cx = (cubeArea.getWidth() - (getColumnCount() * cubeGroup.getTotalCubeSize())) / 2;
        int cy = startingY;
        int numCubes = (int) Math.round(val / cubeGroup.getUnitsPerCube());

        for (int i = 0; i < numCubes; i++) {
            cubeArea.fillRect(cx, cy, cubeSize, cubeSize);
            cy -= cubeGroup.getTotalCubeSize();
            if ((i + 1) % cubeGroup.getNumCubesPerColumn() == 0) {
                cx += cubeGroup.getTotalCubeSize();
                cy = startingY;
            }

        }

        labelArea.setPaint(theme.text());
        frameworkLabel.draw(labelArea, labelArea.getWidth() / 2, 0);
        // If we have extra space, have the gap at the the bottom, rather than awkwardly between the two labels
        valueLabel.draw(labelArea, labelArea.getWidth() / 2, frameworkLabel.getActualHeight());
    }

    public int getColumnCount() {
        return (int) Math.ceil(d.value().getValue() / (cubeGroup.getNumCubesPerColumn() * cubeGroup.getUnitsPerCube()));
    }

    public void decrementFonts() {
        frameworkLabel.setTargetHeight((int) (frameworkLabel.getTargetHeight() * DECREMENT_INCREMENT));
        valueLabel.setTargetHeight((int) (valueLabel.getTargetHeight() * DECREMENT_INCREMENT));
    }

}
