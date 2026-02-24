package io.quarkus.infra.performance.graphics.charts.fonts;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.infra.performance.graphics.Theme;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class Sizer {
    // The g.getGraphics().getFontMetrics().getHeight() number is 39-40% bigger than the notional font size; use it to estimate what font size we might need to set
    private static final double REAL_HEIGHT_RATIO = 1.4;

    private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

    // Dummy graphics, just for font metrics
    private static final SVGGraphics2D g;

    static {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(svgNS, "svg", null);

        g = new SVGGraphics2D(doc);
    }

    private static final Map<Integer, Font> fonts = new HashMap<>();
    private static final Map<Integer, Font> boldFonts = new HashMap<>();

    private static String longestString(Collection<String> strings) {
        return strings.stream()
                .flatMap(s -> Arrays.stream(s.split("\n")))
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }

    public static int calculateWidth(Collection<String> strings, int fontSize) {
        String longest = longestString(strings);
        return calculateWidth(longest, fontSize);
    }

    public static int calculateWidth(String string, int fontSize) {
        // Default to assuming bold fonts, to err on the side of too much whitespace
        return calculateWidth(string, fontSize, BOLD);
    }

    public static int calculateWidth(String string, int fontSize, FontStyle fontStyle) {
        String longest = longestString(List.of(string.split("\n")));
        return calculateWidthNoLineBreaks(longest, fontSize, fontStyle);
    }

    private static int calculateWidthNoLineBreaks(String longest, int fontSize, FontStyle fontStyle) {
        Font font = getFont(fontSize, fontStyle);
        return calculateWidth(longest, font);
    }

    public static int calculateWidth(String longest, Font font) {
        FontMetrics fm = g.getFontMetrics(font);
        return fm.stringWidth(longest);
    }

    private static Font getFont(int fontSize, FontStyle fontStyle) {
        if (fontStyle == BOLD) {
            return boldFonts.computeIfAbsent(fontSize, s -> Theme.FONT.getFont(fontStyle, fontSize));
        } else {
            return fonts.computeIfAbsent(fontSize, s -> Theme.FONT.getFont(fontStyle, fontSize));
        }
    }

    public static int calculateHeight(int fontSize) {
        Font font = getFont(fontSize, PLAIN);
        FontMetrics fm = g.getFontMetrics(font);
        return fm.getHeight();
    }

    public static int calculateFontSize(int targetHeight) {
        int fontSize = (int) (targetHeight / REAL_HEIGHT_RATIO);
        while (calculateHeight(fontSize) > targetHeight) {
            fontSize--;
        }
        while (calculateHeight(fontSize) < targetHeight) {
            fontSize++;
        }
        return fontSize;
    }
}
