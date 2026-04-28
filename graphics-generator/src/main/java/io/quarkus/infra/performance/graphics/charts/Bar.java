package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class Bar extends ScaledElement {
    static final int BAR_THICKNESS = 44;
    public static final int VALUE_LABEL_HEIGHT = BAR_THICKNESS * 2 / 3;
    private static final int MINIMUM_BAR_THICKNESS = 44;
    private static final int MAXIMUM_BAR_THICKNESS = 44;
    private static final int MINIMUM_BAR_LENGTH = 200;

    public static final int LEFT_LABEL_SIZE = Sizer.calculateFontSize(BAR_THICKNESS / 2);
    public static final int LABEL_PADDING = 12;

    private static final int barSpacing = 12;

    private final Label valueLabel;
    private final Label frameworkLabel;
    private final Datapoint d;

    private final String frameworkLabelText;
    private final boolean showFrameworkLabels;
    private boolean isInverted;

    public Bar(Datapoint d, LabelGroup frameworkLabelGroup, LabelGroup valueLabelGroup, ScaleGroup scaleGroup, EmbedOptions embedOptions) {
        super(scaleGroup);
        isInverted = embedOptions.isInverted();
        showFrameworkLabels = embedOptions.showFrameworkLabels();
        this.d = d;
        double val = d.value().getValue();

        frameworkLabelText = showFrameworkLabels ? d.framework().getExpandedName():"";
        frameworkLabel = new Label(frameworkLabelText, frameworkLabelGroup)
                .setVerticalAlignment(VAlignment.MIDDLE)
                .setStyles(new FontStyle[]{BOLD, PLAIN})
                .setTargetHeight(BAR_THICKNESS);

        if (embedOptions.isEmbedded()) {
            frameworkLabel.setHorizontalAlignment(Alignment.CENTER);
        } else {
            frameworkLabel.setHorizontalAlignment(Alignment.RIGHT);
        }

        String valueLabelText = String.format("%d %s", Math.round(val), d.value().getUnits());
        valueLabel = new Label(valueLabelText, valueLabelGroup).setStyle(BOLD).setTargetHeight(VALUE_LABEL_HEIGHT);

        if (isInverted) {
            valueLabel.setHorizontalAlignment(Alignment.RIGHT);
        }

        // This will probably be overridden, but set a value
        offset = Sizer.calculateWidth(frameworkLabelText, LEFT_LABEL_SIZE) + LABEL_PADDING;
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
        return offset + MINIMUM_BAR_LENGTH + getValueLabelWidth() + LABEL_PADDING;
    }

    private int getValueLabelWidth() {
        return valueLabel.calculateWidth();
    }

    public String getLeftLabelText() {
        return frameworkLabelText;
    }

    @Override
    public void draw(Subcanvas barArea, Theme theme) {
        double val = d.value().getValue();
        // Vertically align text with the centre of the bars
        int labelY = barArea.getHeight() / 2;

        barArea.setPaint(theme.text());
        final int frameworkTextWidth = offset - LABEL_PADDING; // Offset includes the label padding,
        int leftLabelX = switch (frameworkLabel.getHorizontalAlignment()) {
            case Alignment.CENTER ->
                    (frameworkTextWidth) / 2; // We want half the text width and half of a label paddings
            case Alignment.RIGHT -> frameworkTextWidth;
            case Alignment.LEFT -> 0;
        };

        Subcanvas frameworkSubcanvas = new Subcanvas(barArea, frameworkTextWidth, frameworkLabel.getTargetHeight(), 0, 0);
        frameworkLabel.draw(frameworkSubcanvas, leftLabelX, labelY);

        int barx = isInverted ? 0:offset;
        Subcanvas barSubcanvas = new Subcanvas(barArea, barArea.getWidth() - offset, frameworkLabel.getTargetHeight(), barx, 0);

        // If this framework isn't found, it will just be the text colour, which is fine
        barSubcanvas.setPaint(theme.chartElements().get(d.framework()));
        int length = (int) (val * scaleGroup.getScale());
        int y = (barArea.getHeight() - BAR_THICKNESS) / 2;

        int x = isInverted ? barSubcanvas.getWidth() - length:0;
        int labelX = isInverted ? (barSubcanvas.getWidth() - length - LABEL_PADDING):length + LABEL_PADDING;

        barSubcanvas.fillRect(x, y, length, BAR_THICKNESS);

        barSubcanvas.setPaint(theme.text());

        valueLabel.setTargetHeight(BAR_THICKNESS * 2 / 3).draw(barSubcanvas, labelX, labelY);
    }

    public int getMaximumBarWidth(Subcanvas barArea) {
        return barArea.getWidth() - offset - LABEL_PADDING - getValueLabelWidth();
    }

    public double getMaximumScale(Subcanvas barArea) {
        return getMaximumBarWidth(barArea) / d.value().getValue();
    }
}
