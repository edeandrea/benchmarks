package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;
import static java.lang.Math.round;

public class Bar implements ElasticElement {
    static final int BAR_THICKNESS = 44;
    public static final int VALUE_LABEL_HEIGHT = BAR_THICKNESS * 2 / 3;
    private static final int MINIMUM_BAR_THICKNESS = 44;
    private static final int MAXIMUM_BAR_THICKNESS = 44;
    private static final int MINUMUM_FONT_SIZE = 8;
    private static final int MINIMUM_BAR_LENGTH = 200;

    public static final int LEFT_LABEL_SIZE = Sizer.calculateFontSize(BAR_THICKNESS / 2);
    public static final int RIGHT_LABEL_SIZE = Sizer.calculateFontSize(VALUE_LABEL_HEIGHT);

    private static final int barSpacing = 12;
    private static final int labelPadding = 12;

    private final String valueLabelText;
    private final Label valueLabel;
    private final Label frameworkLabel;
    private final Datapoint d;

    private double scale = 1;
    private int leftLabelWidth;
    private String frameworkLabelText;

    public Bar(Datapoint d) {
        this.d = d;
        double val = d.value().getValue();
        frameworkLabelText = d.framework().getExpandedName();
        frameworkLabel = new Label(frameworkLabelText)
                .setHorizontalAlignment(Alignment.RIGHT)
                .setVerticalAlignment(VAlignment.MIDDLE)
                .setStyles(new FontStyle[]{BOLD, PLAIN})
                .setTargetHeight(BAR_THICKNESS);
        valueLabelText = String.format("%d %s", round(val), d.value().getUnits());
        valueLabel = new Label(valueLabelText).setStyle(BOLD).setTargetHeight(VALUE_LABEL_HEIGHT);

        // This will probably be overridden, but set a value
        leftLabelWidth = Sizer.calculateWidth(frameworkLabelText, LEFT_LABEL_SIZE);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    // Allows alignment of a group of bars
    public void setLeftLabelWidth(int w) {
        this.leftLabelWidth = w;
    }

    @Override
    public int getMaximumVerticalSize() {
        return MAXIMUM_BAR_THICKNESS + barSpacing;
    }

    @Override
    public int getMaximumHorizontalSize() {
        // Arbitrary; we can go big, but this will contribute to the preferred size
        return getMinimumHorizontalSize() + 1000;
    }

    @Override
    public int getMinimumVerticalSize() {
        return MINIMUM_BAR_THICKNESS + barSpacing;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return leftLabelWidth + MINIMUM_BAR_LENGTH + getValueLabelWidth() + 2 * labelPadding;
    }

    private int getValueLabelWidth() {
        return Sizer.calculateWidth(valueLabelText, RIGHT_LABEL_SIZE, BOLD);
    }

    public String getLeftLabelText() {
        return frameworkLabelText;
    }

    public String getRightLabelText() {
        return valueLabelText;
    }

    @Override
    public void draw(Subcanvas barArea, Theme theme) {
        double val = d.value().getValue();
        // Vertically align text with the centre of the bars
        int labelY = barArea.getHeight() / 2;

        barArea.setPaint(theme.text());

        Subcanvas frameworkSubcanvas = new Subcanvas(barArea, leftLabelWidth, frameworkLabel.getTargetHeight(), 0, 0);
        frameworkLabel.draw(frameworkSubcanvas, leftLabelWidth, labelY);
        int xOffset = frameworkSubcanvas.getWidth() + labelPadding;
        Subcanvas barSubcanvas = new Subcanvas(barArea, barArea.getWidth() - xOffset, frameworkLabel.getTargetHeight(), xOffset, 0);

        // If this framework isn't found, it will just be the text colour, which is fine
        barSubcanvas.setPaint(theme.chartElements().get(d.framework()));
        int length = (int) (val * scale);
        int y = (barArea.getHeight() - BAR_THICKNESS) / 2;
        barSubcanvas.fillRect(0, y, length, BAR_THICKNESS);

        barSubcanvas.setPaint(theme.text());

        valueLabel.setTargetHeight(BAR_THICKNESS * 2 / 3).draw(barSubcanvas, length + labelPadding, labelY);
    }

    public int getMaximumBarWidth(Subcanvas barArea) {
        return barArea.getWidth() - 2 * labelPadding - leftLabelWidth - getValueLabelWidth();
    }

    public double getMaximumScale(Subcanvas barArea) {
        return getMaximumBarWidth(barArea) / d.value().getValue();
    }
}
