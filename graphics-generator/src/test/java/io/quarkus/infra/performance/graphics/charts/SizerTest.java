package io.quarkus.infra.performance.graphics.charts;

import java.util.Set;

import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SizerTest {

    @Test
    void calculateWidthForSingleString() {
        // It's hard to reason about the exact right values here, so hardcode some expectations
        assertEquals(68, Sizer.calculateWidth("some string", 12));
        assertEquals(102, Sizer.calculateWidth("some string", 18));
    }

    @Test
    void calculateWidthForSingleStringWithStyle() {
        // It's hard to reason about the exact right values here, so hardcode some expectations
        assertEquals(65, Sizer.calculateWidth("some string", 12, PLAIN));
        assertEquals(68, Sizer.calculateWidth("some string", 12, BOLD));
        assertEquals(95, Sizer.calculateWidth("some string", 18, PLAIN));
        assertEquals(102, Sizer.calculateWidth("some string", 18, BOLD));
    }

    @Test
    void calculateWidthForCollectionOfStrings() {
        assertEquals(Sizer.calculateWidth("some string", 12),
                Sizer.calculateWidth(Set.of("short", "some string", "tiddly"), 12));
    }

    @Test
    void calculateWidthForCollectionOfStringsWithLineBreaks() {
        assertEquals(Sizer.calculateWidth("some string", 12),
                Sizer.calculateWidth(Set.of("short\ns", "yup\nsome string\nok", "tiddly\nwinks"), 12));
    }

    @Test
    void calculateHeight() {
        // The actual space occupied by a font is bigger than the notional size, because of descents and other font parts
        assertEquals(17, Sizer.calculateHeight(12));
    }

    @Test
    void calculateFontSize() {
        assertEquals(12, Sizer.calculateFontSize(17));
    }

}
