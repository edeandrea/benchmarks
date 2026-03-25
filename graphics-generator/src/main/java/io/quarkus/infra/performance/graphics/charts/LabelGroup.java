package io.quarkus.infra.performance.graphics.charts;

import java.awt.Font;
import java.util.Arrays;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class LabelGroup {
    public static final int DEFAULT_FONT_SIZE = 10; // An arbitrary default, which will almost certainly change for most usages
    private Font baseFont;
    private FontStyle[] styles;
    private Font[] styledFonts;

    public LabelGroup(int size) {
        baseFont = Theme.FONT.getFont(PLAIN, size);
    }

    public LabelGroup() {
        this(DEFAULT_FONT_SIZE);
    }

    public int getFontSize() {
        return baseFont != null ? baseFont.getSize():0;
    }

    public Font getBaseFont() {
        return baseFont;
    }

    public void setBaseFont(Font baseFont) {
        this.baseFont = baseFont;
        // Invalidate cache when base font changes
        if (styles != null) {
            updateFontCache();
        }
    }

    public void setStyles(FontStyle[] styles) {
        this.styles = styles;
        if (baseFont != null) {
            updateFontCache();
        }
    }

    private void updateFontCache() {
        styledFonts = Arrays.stream(styles)
                .map(style -> Theme.FONT.getFont(style, baseFont.getSize()))
                .toArray(Font[]::new);
    }

    public Font getFont(int index) {
        if (styledFonts == null || styledFonts.length == 0) {
            return baseFont;
        }
        return styledFonts[index % styledFonts.length];
    }

    public void decrement() {
        int newSize = getFontSize() - 1;
        setBaseFont(Theme.FONT.getFont(PLAIN, newSize));

        // Note that if this is called, the owning label's target height is inaccurate
    }
}
