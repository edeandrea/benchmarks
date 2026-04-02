package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InlinedSVGLink extends InlinedSVG {
    private final String text;
    private String url;

    public InlinedSVGLink(String url, String s, int x, int y) {
        super(x, y);
        this.url = url;
        this.text = s;
    }

    public void draw(SAXSVGDocumentFactory factory, Element root, Document doc, Theme theme) {
        // Create <a> element
        Element link = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "a");
        link.setAttributeNS(
                SVGConstants.XLINK_NAMESPACE_URI,
                "xlink:href",
                url
        );

// Create <text> element
        Element textElement = doc.createElementNS(
                SVGConstants.SVG_NAMESPACE_URI,
                SVGConstants.SVG_TEXT_TAG
        );

        textElement.setAttributeNS(null, "x", String.valueOf(x));
        textElement.setAttributeNS(null, "y", String.valueOf(y));
        textElement.setAttributeNS(null, "fill", "#000000"); // make the text invisible, but still accessible to text readers
        textElement.setAttributeNS(null, "fill-opacity", "0");
        textElement.setAttributeNS(null, "stroke", "none"); // without this, the colours and other styling has no effect
        textElement.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", "preserve"); // otherwise spaces are collapsed and the hot area is too small

        textElement.setTextContent(text);

// Append text to link
        link.appendChild(textElement);

// Append link to root SVG
        root.appendChild(link);
    }
}
