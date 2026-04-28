package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.ITALIC;

public class Title implements ElasticElement {
    protected static final int MINIMUM_TITLE_TEXT_SIZE = 12;
    protected static final int MAXIMUM_TITLE_TEXT_SIZE = 48;
    public static final int RATIO = 2;
    protected static final int MINIMUM_SUBTITLE_TEXT_SIZE = MINIMUM_TITLE_TEXT_SIZE / RATIO;
    protected static final int MAXIMUM_SUBTITLE_TEXT_SIZE = MAXIMUM_TITLE_TEXT_SIZE / 2;
    protected final String title;
    protected final String subtitle;

    private final Label titleLabel;
    private final Label subtitleLabel;

    public Title(String title, EmbedOptions embedOptions) {
        this(title, "", embedOptions);
    }

    public Title(String title, String subtitle, EmbedOptions embedOptions) {
        this.title = title;
        titleLabel = new Label(title).setStyle(BOLD).setVerticalAlignment(VAlignment.TOP);
        this.subtitle = subtitle;
        subtitleLabel = new Label(subtitle).setStyle(ITALIC).setVerticalAlignment(VAlignment.TOP);

        if (embedOptions.isInverted()) {
            titleLabel.setHorizontalAlignment(Alignment.RIGHT);
            subtitleLabel.setHorizontalAlignment(Alignment.RIGHT);
        }
    }

    @Override
    public int getMaximumVerticalSize() {
        // Assume top and bottom margins equal to half the title height (each)
        int subtitleHeight = hasSubtitle() ? Sizer.calculateHeight(MAXIMUM_SUBTITLE_TEXT_SIZE):0;
        return 2 * Sizer.calculateHeight(MAXIMUM_TITLE_TEXT_SIZE) + subtitleHeight;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return 2 * Math.max(Sizer.calculateWidth(title, MAXIMUM_TITLE_TEXT_SIZE),
                Sizer.calculateWidth(subtitle, MAXIMUM_TITLE_TEXT_SIZE));
    }

    @Override
    public int getMinimumVerticalSize() {
        // Assume top and bottom margins equal to half the title height (each)
        int subtitleHeight = hasSubtitle() ? Sizer.calculateHeight(MINIMUM_SUBTITLE_TEXT_SIZE):0;
        return 2 * Sizer.calculateHeight(MINIMUM_TITLE_TEXT_SIZE) + subtitleHeight;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return 2 * Math.max(Sizer.calculateWidth(title, MINIMUM_TITLE_TEXT_SIZE),
                Sizer.calculateWidth(subtitle, MINIMUM_TITLE_TEXT_SIZE));
    }

    @Override
    public void draw(Subcanvas g, Theme theme) {
        int subtitleFactor = hasSubtitle() ? 1:0;
        // Allow margins of half the main title height
        int titleHeight = (RATIO * g.getHeight()) / (2 * RATIO + subtitleFactor);
        titleLabel.setTargetHeight(titleHeight);
        int topMargin = titleHeight / 2;

        int x = titleLabel.getHorizontalAlignment().equals(Alignment.RIGHT) ? g.getWidth():0;
        titleLabel.draw(g, x, topMargin);

        if (hasSubtitle()) {
            int subtitleHeight = titleHeight / RATIO;
            subtitleLabel.setTargetHeight(subtitleHeight);
            subtitleLabel.draw(g, x, topMargin + titleHeight);
        }
    }

    private boolean hasSubtitle() {
        return subtitle != null && subtitle.trim().length() > 0;
    }
}
