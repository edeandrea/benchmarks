package io.quarkus.infra.performance.graphics;

import static io.quarkus.infra.performance.graphics.SvgAdjuster.adjustSvg;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiFunction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.quarkus.infra.performance.graphics.charts.Chart;
import io.quarkus.infra.performance.graphics.charts.InlinedSVG;
import io.quarkus.infra.performance.graphics.charts.Subcanvas;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

@ApplicationScoped
public class ImageGenerator {
    private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

    @Inject
    PngExporter pngExporter;

    public void generate(BiFunction<PlotDefinition, BenchmarkData, Chart> chartConstructor, BenchmarkData data,
                         PlotDefinition plotDefinition, Path outFile, Theme theme)
            throws IOException {
        if (data != null && data.results() != null) {
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            Document doc = impl.createDocument(svgNS, "svg", null);

            Chart chart = chartConstructor.apply(plotDefinition, data);

            SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
            svgGenerator.setSVGCanvasSize(
                    new Dimension(chart.getPreferredHorizontalSize(), chart.getPreferredVerticalSize())
            );
            chart.draw(new Subcanvas(svgGenerator), theme);

            Element root = svgGenerator.getRoot();
            initialiseFonts(doc, root);
            inlineGraphics(doc, root, chart.getInlinedSVGs(), theme);

            Files.createDirectories(outFile.getParent());

            try (var buffer = new StringWriter()) {
              svgGenerator.stream(root, buffer, true, false);
              var svg = buffer.toString();
              var adjusted = adjustSvg(svg);
              Files.writeString(outFile, adjusted);
            }

            this.pngExporter.exportAsPng(outFile);
        } else {
            System.out.printf("\uD83D\uDDD1️ Not generating image for %s (no data)\n", outFile.toAbsolutePath());

        }
    }

    private void inlineGraphics(Document doc, Element root, Collection<InlinedSVG> inlinedSVGs, Theme theme) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

        for (InlinedSVG inlinedSVG : inlinedSVGs) {
            inlinedSVG.draw(factory, root, doc, theme);
        }
    }

    private static void initialiseFonts(Document doc, Element root) {
        Element style = doc.createElementNS(svgNS, "style");
        style.setTextContent(Theme.FONT.getCss());
        root.setAttribute("font-family", Theme.FONT.getFamilyDeclaration());
        root.insertBefore(style, root.getFirstChild());

    }

}
