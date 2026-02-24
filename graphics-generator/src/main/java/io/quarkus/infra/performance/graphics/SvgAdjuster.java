package io.quarkus.infra.performance.graphics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class SvgAdjuster {

    public static final String GRAPHICS_ELEMENT = "g";
    public static final String STYLE = "style";
    public static final String FONT_FAMILY_DECLARATION = "font-family:\\s*'" + Theme.FONT.getName() + "'";

    public static String adjustSvg(String svg) {
        Document svgDoc = Jsoup.parse(svg, "", Parser.xmlParser());

        Elements gElements = svgDoc.select(GRAPHICS_ELEMENT);

        for (String name : Theme.FONT.getNames()) {
            String fontFamilyDeclaration = "font-family:\\s*'" + name + "'";
            String fallbackString = "font-family: '" + name + "', " + Theme.FONT.getFamilyDeclaration();
            for (Element g : gElements) {
                String style = g.attr(STYLE);
                style = style.replaceAll(fontFamilyDeclaration, fallbackString);
                g.attr(STYLE, style);
            }
        }
        return svgDoc.outerHtml();
    }
}
