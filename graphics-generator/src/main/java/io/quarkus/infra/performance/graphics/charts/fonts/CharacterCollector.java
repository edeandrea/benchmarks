package io.quarkus.infra.performance.graphics.charts.fonts;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collects all characters used in chart labels to enable precise font subsetting.
 * Tracks which characters are used with which font styles for optimal subsetting.
 */
public class CharacterCollector {

    private static final Map<FontStyle, Set<Character>> charactersByStyle = Collections.synchronizedMap(new HashMap<>());

    /**
     * Register text that will be rendered in charts with a specific font style.
     */
    public static void registerText(String text, FontStyle style) {
        if (text != null && style != null) {
            charactersByStyle.computeIfAbsent(style, k -> Collections.synchronizedSet(new HashSet<>()));
            for (char c : text.toCharArray()) {
                charactersByStyle.get(style).add(c);
            }
        }
    }

    /**
     * Register multiple text strings with a specific font style.
     */
    public static void registerText(String[] texts, FontStyle style) {
        if (texts != null && style != null) {
            for (String text : texts) {
                registerText(text, style);
            }
        }
    }

    /**
     * Register text with default (PLAIN) style.
     */
    public static void registerText(String text) {
        registerText(text, FontStyle.PLAIN);
    }

    /**
     * Get characters used with a specific font style.
     */
    public static Set<Character> getCharacters(FontStyle style) {
        Set<Character> chars = charactersByStyle.get(style);
        return chars != null ? new HashSet<>(chars) : new HashSet<>();
    }

    /**
     * Clear all collected characters.
     */
    public static void reset() {
        charactersByStyle.clear();
    }
}
