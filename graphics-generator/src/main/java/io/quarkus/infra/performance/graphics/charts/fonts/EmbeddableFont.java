package io.quarkus.infra.performance.graphics.charts.fonts;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.ITALIC;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class EmbeddableFont {

    //             Java GraphicsEnvironment needs a ttf font, not a woff, so read from the github repo
    // AWT has poor support for variable fonts. For width calculations other than bold and plain, we need to use the named variant of the fonts
    // (Even with the richer weights in TextAttributes, the graphics engine will fall back to the standard widths)

    public static final EmbeddableFont OPENSANS = new EmbeddableFont("Open Sans", List.of("Arial", "Noto Sans", "sans-serif"),
            Map.of(
                PLAIN, "https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-Light.ttf",
                BOLD, "https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-SemiBold.ttf",
                ITALIC, "https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-LightItalic.ttf"
            ));
    private String css;
    private final String familyDeclaration;
    private final String fontName;
    private final Map<FontStyle, Font> fonts;
    private final List<DownloadedFont> downloadedFonts;
    private boolean isSubsetted = false;

    private record DownloadedFont(Font font, byte[] raw, FontStyle style) {
    }

    private EmbeddableFont(String fontName, List<String> fallbacks, Map<FontStyle, String> fontUrls) {

        this.fontName = fontName;

        this.downloadedFonts = fontUrls.entrySet().stream().map(entry -> loadAndRegisterFont(entry.getValue(), entry.getKey()))
                .sorted((a, b) -> a.font.getFontName().compareTo(b.font.getFontName()))
                .collect(Collectors.toList());

        fonts = downloadedFonts.stream().collect(Collectors.toMap(d -> d.style, DownloadedFont::font));

        familyDeclaration = "'" + fontName + " Light', '" + fontName + "', " + fallbacks.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")).replaceAll("'sans-serif'", "sans-serif");

    }

    private DownloadedFont loadAndRegisterFont(String fontUrl, FontStyle style) {
        try {
            // Download the full TTF font file
            byte[] fullTtfBytes = downloadFont(fontUrl);

            Font font;
            try (InputStream stream = new ByteArrayInputStream(fullTtfBytes)) {
                font = Font.createFont(Font.TRUETYPE_FONT, stream);
                if (style == ITALIC) {
                    // Just for italics, we also need to add the metadata saying the font is italic; if we do it for bold, width calculations are based on an artificially fat width and everything is misaligned
                    font = font.deriveFont(Font.ITALIC);
                }
            }

            // To make fonts work, we need a css declaration (below), and we also need to tell Java about the font (done here)
            GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .registerFont(font);

            // Don't subset yet - wait until we know which characters are actually used
            return new DownloadedFont(font, fullTtfBytes, style);
        } catch (URISyntaxException | IOException | FontFormatException e) {
            throw new RuntimeException("Failed to load font from " + fontUrl, e);
        }
    }


    public String[] getNames() {
        return fonts.values().stream().map(Font::getName).toList().toArray(new String[0]);
    }

    /**
     * Gets a font instance with the specified style and size. This should be used instead of new Font(), partly for better control of styles,
     * but also because on Linux, there's a metrics difference between fonts created using new Font() and fonts created with deriveFont().
     * That can cause wonky spacing on CI-generated images.
     */
    public Font getFont(FontStyle style, float size) {
        // If the style doesn't exist, fall back to the first font we find
        return fonts.get(style).deriveFont(size);
    }

    public String getCss() {
        // Lazy subsetting - only do it when CSS is actually needed
        if (!isSubsetted) {
            performSubsetting();
            isSubsetted = true;
            // Reset the collector after subsetting so it's ready for the next chart
            CharacterCollector.reset();
        }
        return css;
    }

    private void performSubsetting() {
        try {
            List<SubsettedFont> subsettedFonts = new ArrayList<>();
            for (DownloadedFont dfont : downloadedFonts) {
                // Get characters used with this specific font style
                Set<Character> usedCharacters = CharacterCollector.getCharacters(dfont.style);

                // Subset the TTF to only include characters used with this style
                byte[] subsettedTtfBytes = FontSubsetter.subsetToCharacters(dfont.raw, usedCharacters);

                subsettedFonts.add(new SubsettedFont(dfont.font, subsettedTtfBytes));
            }

            css = subsettedFonts.stream()
                    .map(EmbeddableFont::generateFontFaceCSS)
                    .collect(Collectors.joining(" "));
        } catch (IOException e) {
            throw new RuntimeException("Failed to subset fonts", e);
        }
    }

    private record SubsettedFont(Font font, byte[] subsetted) {
    }

    public String getName() {
        return fontName;
    }

    private static byte[] downloadFont(String fontUrl) throws URISyntaxException, IOException {
        // Determine cache directory inside build folder
        Path cacheDir = Paths.get("target", "fonts");
        Files.createDirectories(cacheDir);

        // Derive a safe file name from the URL
        String fileName = Paths.get(new URI(fontUrl).getPath()).getFileName().toString();
        Path cachedFont = cacheDir.resolve(fileName);

        // If cached font exists, load from disk
        if (Files.exists(cachedFont)) {
            return Files.readAllBytes(cachedFont);
        }

        // Otherwise download and cache it
        byte[] data;
        try (InputStream in = new URI(fontUrl).toURL().openStream()) {
            data = in.readAllBytes();
        }

        // Save to cache
        Files.write(cachedFont, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return data;
    }

    private static String generateFontFaceCSS(SubsettedFont subsettedFont) {
        // Base64 encode the subsetted TTF font bytes
        String base64Font = Base64.getEncoder().encodeToString(subsettedFont.subsetted());
        String fontName = subsettedFont.font().getFontName();
        return """
                  @font-face {
                    font-family: '%s';
                    src:
                      url('data:font/truetype;base64,%s') format('truetype'),
                      local(%s),
                      local(%s);
                    unicode-range: U+0020-007E, U+00A0-00FF;
                    font-style: normal;
                  }
                """.formatted(fontName, base64Font, fontName, fontName.replaceAll(" ", ""));
    }

    public String getFamilyDeclaration() {
        return familyDeclaration;
    }


}
