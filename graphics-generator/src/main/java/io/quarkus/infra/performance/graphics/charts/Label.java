package io.quarkus.infra.performance.graphics.charts;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Arrays;
import java.util.Comparator;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.VAlignment;

public class Label {

    public static final String LINE_BREAK = "\n";
    private static final int DEFAULT_TARGET_HEIGHT = 24;

    private final String[] strings;
    private int targetHeight = 24; // Arbitrary default
    private double lineSpacing = 1;
    private DelimitedStyles styles = new DelimitedStyles(new int[]{Font.PLAIN}, LINE_BREAK);
    private Alignment alignment = Alignment.LEFT;
    private VAlignment valignment = VAlignment.MIDDLE;
    private FontMetrics fontMetrics;
    private Font baseFont;
    private Font[] fonts;

    /**
     * @param text Use \n for multiline text
     */
    public Label(String text) {
        this(text.split(LINE_BREAK));

    }

    public Label(String[] lines) {
        this.strings = lines;
        setTargetHeight(DEFAULT_TARGET_HEIGHT);
    }

    public void draw(Subcanvas g) {
        draw(g, 0, 0);
    }

    public void draw(Subcanvas g, int x, int y) {

        g.getGraphics().setFont(baseFont);

        // The SVG attribute alignment-baseline="middle" is not supported by Batik.
        // The value we pass in to drawString is the position of the bottom baseline

        // Four variables describe the height of a font: leading (pronounced like the metal), ascent, descent, and height. Leading is the amount of space required between lines of the same font. Ascent is the space above the baseline required by the tallest character in the font. Descent is the space required below the baseline by the lowest descender (the "tail" of a character like "y"). Height is the total of the three: ascent, baseline, and descent.

        // Should be the same as the targetHeight, but recalculate in case of rounding errors
        fontMetrics = g.getGraphics().getFontMetrics();
        int lineHeight = (int) (fontMetrics.getHeight() * lineSpacing);
        int textBlockHeight = lineHeight * strings.length;

        // Compute starting y to vertically center the text block
        int yPosition = switch (valignment) {
            case TOP -> y + fontMetrics.getAscent();
            case BOTTOM -> y;
            case MIDDLE -> y - textBlockHeight / 2 + fontMetrics.getAscent();
        };
        
        for (int i = 0; i < strings.length; i++) {

            String string = strings[i];
            // Set a base font for the line height
            Font font = fonts[i % fonts.length];
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);

            // String bounds is a bit more accurate than getWidth() for alignment
            int width = (int) Math.round(metrics.getStringBounds(string, g.getGraphics()).getWidth());

            int alignedX = switch (alignment) {
                case LEFT -> x;
                case RIGHT -> x - width;
                case CENTER -> x - width / 2;
            };

            // Now we may need to split further; if the delimiter was a line-break, this will be a no-op
            String[] segments = string.split(styles.delimiter());
            int segmentX = alignedX;

            for (int j = 0; j < segments.length; j++) {
                // Chaos; which index we use depends on the delimiter
                int index = (LINE_BREAK.equals(styles.delimiter())) ? i:j;
                font = fonts[index % fonts.length];
                g.getGraphics().setFont(font);
                metrics = g.getGraphics().getFontMetrics(font);

                String segment = segments[j];
                // Add back the delimiter, except at the end
                if (j < segments.length - 1) {
                    segment += styles.delimiter();
                }
                // String bounds is a bit more accurate than getWidth() for alignment
                int segmentWidth = (int) Math.round(metrics.getStringBounds(segment, g.getGraphics()).getWidth());

                g.drawString(segment, segmentX, yPosition);
                segmentX += segmentWidth;
            }
            yPosition += metrics.getHeight() * lineSpacing; // Recalculate the line height in case the style affected the height slighly

        }


    }

    int getAscent() {
        return fontMetrics.getAscent();
    }

    // We will need to have a labelGroup abstraction to keep sizes consistent, and perhaps also set a maximum width
    public Label setTargetHeight(int height) {
        this.targetHeight = height;
        int size = strings.length > 1 ? Sizer.calculateFontSize((int) (targetHeight / (strings.length * lineSpacing))):Sizer.calculateFontSize(targetHeight);
        baseFont = new Font(Theme.FONT.getName(), styles.styles()[0], size);
        initialiseFonts();
        return this;
    }

    private void initialiseFonts() {
        fonts = Arrays.stream(styles.styles())
                .mapToObj(s -> new Font(Theme.FONT.getName(), s, baseFont.getSize()))
                .toArray(Font[]::new);
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public Label setHorizontalAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Should be one of the constants declared in #Font or their combination
     */
    public Label setStyle(int style) {
        return setStyles(new int[]{style});
    }

    /**
     * Should be one of the constants declared in #Font or their combination
     * The different styles are applied to different parts of the text, with the default delimiter being \n
     */
    public Label setStyles(int[] styles) {
        return setStyles(styles, LINE_BREAK);
    }


    /*
     *    Note that if a style delimiter is set (other than a line break), line breaks will no longer be style delimiters.
     */
    public Label setStyles(int[] styles, String newDelimiter) {

        this.styles = new DelimitedStyles(styles, newDelimiter);

        initialiseFonts();

        return this;
    }

    public Label setVerticalAlignment(VAlignment vAlignment) {
        this.valignment = vAlignment;
        return this;
    }

    public int getLineHeight() {
        if (fontMetrics != null) {
            return fontMetrics.getHeight();
        } else {
            return Sizer.calculateHeight(baseFont.getSize());
        }
    }

    public int calculateWidth(String s) {
        if (fontMetrics != null) {
            return fontMetrics.stringWidth(s);
        } else {
            return Sizer.calculateWidth(s, baseFont.getSize());
        }
    }


    public int getActualHeight() {
        return (int) (strings.length * lineSpacing * getLineHeight());
    }

    public int getDescent() {
        return fontMetrics.getDescent();
    }

    public int calculateWidth() {
        String longestText = getLongestText();
        int index = Arrays.asList(strings).indexOf(longestText);
        Font font = fonts[index % fonts.length];


        return Sizer.calculateWidth(longestText, font);
    }


    public double calculateWidth(int fontSize) {
        String longestText = getLongestText();
        return Sizer.calculateWidth(longestText, fontSize);
    }


    private String getLongestText() {
        // It would be cheaper to just count characters, but sometimes the longest string isn't the fattest – including for framework labels we are using
        return Arrays.stream(strings).max(Comparator.comparingInt(this::calculateWidth)).orElse("");
    }

    public String toString() {
        return "Label[" + getLongestText() + "]";
    }

    public void setLineSpacing(int l) {
        this.lineSpacing = l;
    }


    private record DelimitedStyles(int[] styles, String delimiter) {
    }

}
