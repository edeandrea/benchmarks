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
                PLAIN, new FontUrls("https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-Light.ttf",
                    "https://fonts.gstatic.com/s/opensans/v44/memSYaGs126MiZpBA-UvWbX2vVnXBbObj2OVZyOOSr4dVJWUgsiH0B4gaVI.woff2"),
                BOLD, new FontUrls("https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-SemiBold.ttf",
                    "https://fonts.gstatic.com/s/opensans/v44/memSYaGs126MiZpBA-UvWbX2vVnXBbObj2OVZyOOSr4dVJWUgsgH1x4gaVI.woff2"),
                ITALIC, new FontUrls("https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/ttf/OpenSans-LightItalic.ttf",
                    "https://fonts.gstatic.com/s/opensans/v44/memQYaGs126MiZpBA-UFUIcVXSCEkx2cmqvXlWq8tWZ0Pw86hd0Rk5hkWVAewA.woff2")
            ));
    private final String css;
    private final String familyDeclaration;
    private final String fontName;
    private final Map<FontStyle, Font> fonts;

    private record FontUrls(String ttf, String woff2) {
    }

    private record DownloadedFont(Font font, byte[] raw, byte[] woff2, FontStyle style) {
    }

    private EmbeddableFont(String fontName, List<String> fallbacks, Map<FontStyle, FontUrls> fontUrls) {

        this.fontName = fontName;

        List<DownloadedFont> dfonts = fontUrls.entrySet().stream().map(entry -> loadAndRegisterFont(entry.getValue(), entry.getKey()))
                .sorted((a, b) -> a.font.getFontName().compareTo(b.font.getFontName()))
                .collect(Collectors.toList());

        css = dfonts.stream().map(EmbeddableFont::generateFontFaceCSS).collect(Collectors.joining(" "));
        fonts = dfonts.stream().collect(Collectors.toMap(d -> d.style, DownloadedFont::font));

        familyDeclaration = "'" + fontName + " Light', '" + fontName + "', " + fallbacks.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")).replaceAll("'sans-serif'", "sans-serif");

    }

    private DownloadedFont loadAndRegisterFont(FontUrls fontUrls, FontStyle style) {
        try {
            // Download the TTF font file for Java
            byte[] ttfBytes = downloadFont(fontUrls.ttf());

            // Download the WOFF2 font file for embedding in SVG
            byte[] woff2Bytes = downloadFont(fontUrls.woff2());

            Font font;
            try (InputStream stream = new ByteArrayInputStream(ttfBytes)) {
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


            return new DownloadedFont(font, ttfBytes, woff2Bytes, style);
        } catch (URISyntaxException | IOException | FontFormatException e) {
            throw new RuntimeException("Failed to load font from " + fontUrls.ttf() + " / " + fontUrls.woff2(), e);
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
        return css;
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

    private static String generateFontFaceCSS(DownloadedFont downloadedFont) {
        // To try out the fallback visually, just mangle what goes into the url: line

        // Base64 encode the WOFF2 font bytes (much smaller than TTF)
        // It would be nice to subset the characters and only include what's needed, to save file sizes even more
        String base64Font = Base64.getEncoder().encodeToString(downloadedFont.woff2());
        String fontName = downloadedFont.font().getFontName();
        return """
                  @font-face {
                    font-family: '%s';
                    src:
                      url('data:font/woff2;base64,%s') format('woff2'),
                      local(%s),
                      local(%s);
                    unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
                    font-style: normal;
                  }
                """.formatted(fontName, base64Font, fontName, fontName.replaceAll(" ", ""));
    }

    public String getFamilyDeclaration() {
        return familyDeclaration;
    }


}
