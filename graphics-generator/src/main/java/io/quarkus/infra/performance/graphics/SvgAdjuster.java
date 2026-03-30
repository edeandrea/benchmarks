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

            // Avoid duplication: check if the name is already at the start of the family declaration
            String familyDecl = Theme.FONT.getFamilyDeclaration();
            String fallbackString;
            if (familyDecl.startsWith("'" + name + "'")) {
                // Name is already first in the declaration, don't prepend it
                fallbackString = "font-family: " + familyDecl;
            } else {
                // Name is not in the declaration, prepend it
                fallbackString = "font-family: '" + name + "', " + familyDecl;
            }

            for (Element g : elements) {
                String style = g.attr(STYLE);
                style = style.replaceAll(fontFamilyDeclaration, fallbackString);
                g.attr(STYLE, style);
            }
        }
    }
}
