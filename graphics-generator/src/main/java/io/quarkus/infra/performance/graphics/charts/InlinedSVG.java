package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.Theme;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class InlinedSVG {
    protected int x;
    protected int y;

    public InlinedSVG(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void draw(SAXSVGDocumentFactory factory, Element root, Document doc, Theme theme);
}
