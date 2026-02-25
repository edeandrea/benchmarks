package io.quarkus.infra.performance.graphics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class SvgAdjuster {

    public static final String STYLE = "style";

    public static String adjustSvg(String svg) {
        Document svgDoc = Jsoup.parse(svg, "", Parser.xmlParser());

        // Some of the font information (for bold) is on the g elements, and some (for light and italic) is on the text elements
        addFallbackFonts(svgDoc, "text");
        addFallbackFonts(svgDoc, "g");

        return svgDoc.outerHtml();
    }

    private static void addFallbackFonts(Document svgDoc, String selector) {
        Elements elements = svgDoc.select(selector);
        for (String name : Theme.FONT.getNames()) {
            String fontFamilyDeclaration = "font-family:\\s*'" + name + "'";
            String fallbackString = "font-family: '" + name + "', " + Theme.FONT.getFamilyDeclaration();
            for (Element g : elements) {
                String style = g.attr(STYLE);
                style = style.replaceAll(fontFamilyDeclaration, fallbackString);
                g.attr(STYLE, style);
            }
        }
    }
}
