package io.quarkus.infra.performance.graphics.charts;

import java.io.IOException;
import java.io.InputStream;

import io.quarkus.infra.performance.graphics.Theme;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class InlinedSVGImage extends InlinedSVG {
    private final String resourcePath;
    private final int height;


    public InlinedSVGImage(String path, int height, int x, int y) {
        super(x, y);
        this.resourcePath = path;
        this.height = height;

    }

    public void draw(SAXSVGDocumentFactory factory, Element root, Document doc, Theme theme) {
        // Load SVG from classpath
        try (InputStream is = this.getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            SVGDocument sub = factory.createSVGDocument(null, is);

            // import the <svg> element
            Element imported = (Element) doc.importNode(sub.getDocumentElement(), true);

            // Define a viewbox so scaling works
            String originalWidth = imported.getAttribute("width");
            String originalHeight = imported.getAttribute("height");
            imported.setAttribute("viewBox",
                    String.format("0 0 %s %s", originalWidth, originalHeight));

            // Redefine the height to the one we want
            imported.setAttribute("height",
                    String.valueOf(height));
            // Adjust the width as well, or positioning goes wonky
            imported.setAttribute("width",
                    String.valueOf((Integer.parseInt(originalHeight) * height) / Integer.parseInt(originalHeight)));

            String fillColour = "#" + Integer.toHexString(theme.text().getRGB()).substring(2);
            imported.getElementsByTagName("path").item(0).getAttributes().getNamedItem("fill").setTextContent(fillColour);
            
            // Set a position on the canvas
            imported.setAttribute("x",
                    String.valueOf(x));

            imported.setAttribute("y",
                    String.valueOf(y));

            // append to main document
            root.appendChild(imported);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
