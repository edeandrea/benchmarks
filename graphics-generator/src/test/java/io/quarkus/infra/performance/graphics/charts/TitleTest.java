package io.quarkus.infra.performance.graphics.charts;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleTest extends ElasticElementTest {

    @Test
    public void titleIncludesTitleText() {
        String titleText = "my cool chart";
        var t = new Title(titleText, Optional.empty());
        String s = drawSvg(t);
        assertTrue(s.contains(titleText), s);
    }

    @Test
    public void titleIncludesSubTitleText() {
        String titleText = "my cool chart";
        String subtitleText = "this is the explanation";
        var t = new Title(titleText, subtitleText, Optional.empty());
        String s = drawSvg(t);
        assertTrue(s.contains(titleText), s);
        assertTrue(s.contains(subtitleText), s);
    }
}