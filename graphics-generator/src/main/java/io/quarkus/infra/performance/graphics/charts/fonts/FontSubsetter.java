package io.quarkus.infra.performance.graphics.charts.fonts;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.TTFSubsetter;
import org.apache.pdfbox.io.RandomAccessReadBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility for subsetting fonts to include only the characters needed for charts.
 */
public class FontSubsetter {

    /**
     * Subset a TTF font to include only the specified characters.
     *
     * @param ttfBytes the original TTF font bytes
     * @param characters the set of characters to include in the subset
     * @return the subsetted TTF font bytes
     * @throws IOException if subsetting fails
     */
    public static byte[] subsetToCharacters(byte[] ttfBytes, Set<Character> characters) throws IOException {
        TTFParser parser = new TTFParser();

        try (RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(ttfBytes);
             TrueTypeFont font = parser.parse(buffer)) {

            // Build the list of glyph names for the characters we want to keep
            List<String> glyphNames = new ArrayList<>();

            // Add .notdef glyph (required)
            glyphNames.add(".notdef");

            // Get the character-to-glyph-id mapping table
            var cmap = font.getUnicodeCmapLookup();
            var postScript = font.getPostScript();

            if (cmap != null && postScript != null) {
                // Add glyphs for each character we need
                for (char c : characters) {
                    int glyphId = cmap.getGlyphId(c);
                    if (glyphId > 0 && glyphId < font.getNumberOfGlyphs()) {
                        try {
                            String glyphName = postScript.getName(glyphId);
                            if (glyphName != null && !glyphNames.contains(glyphName)) {
                                glyphNames.add(glyphName);
                            }
                        } catch (Exception e) {
                            // Skip if we can't get the glyph name
                        }
                    }
                }
            }

            TTFSubsetter subsetter = new TTFSubsetter(font, glyphNames);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            subsetter.writeToStream(output);

            return output.toByteArray();
        }
    }
}
