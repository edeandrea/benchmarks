package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.charts.fonts.CharacterCollector;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabelTest extends ElasticElementTest {

    @BeforeEach
    void setUp() {
        CharacterCollector.reset();
    }

    @AfterEach
    void tearDown() {
        CharacterCollector.reset();
    }
    @Test
    public void setTargetHeightInSingleLineCase() {
        Label label = new Label("First");
        int target = 40;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }

    @Test
    public void setTargetHeightInMultilineCase() {
        Label label = new Label("First\nSecond\nThird");
        // This value is chosen so that rounding errors don't matter
        int target = 42;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }

    @Test
    public void setTargetHeightInMultilineCaseWithLineSpacing() {
        Label label = new Label("First\nSecond\nThird");
        label.setLineSpacing(3);
        // A value for which rounding errors don't come into play
        int target = 81;
        label.setTargetHeight(target);
        assertEquals(target, label.getActualHeight());
    }

    @Test
    public void testCharacterRegistrationWithDefaultStyle() {
        CharacterCollector.reset();
        new Label("Hello World");

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertTrue(plainChars.contains('H'));
        assertTrue(plainChars.contains('e'));
        assertTrue(plainChars.contains(' '));
        assertTrue(plainChars.contains('W'));
    }

    @Test
    public void testCharacterRegistrationWithMultilineDefaultStyle() {
        CharacterCollector.reset();
        new Label("First\nSecond\nThird");

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertTrue(plainChars.contains('F'));
        assertTrue(plainChars.contains('S'));
        assertTrue(plainChars.contains('T'));
        // Line breaks are delimiters, not characters in the text
    }

    @Test
    public void testCharacterRegistrationWithBoldStyle() {
        CharacterCollector.reset();
        Label label = new Label("Bold Text");
        label.setStyle(FontStyle.BOLD);

        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);
        assertTrue(boldChars.contains('B'));
        assertTrue(boldChars.contains('o'));
        assertTrue(boldChars.contains('T'));
    }

    @Test
    public void testCharacterRegistrationWithMultipleStyles() {
        CharacterCollector.reset();
        Label label = new Label(new String[]{"Plain Line", "Bold Line", "Italic Line"});
        label.setStyles(new FontStyle[]{FontStyle.PLAIN, FontStyle.BOLD, FontStyle.ITALIC});

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);
        Set<Character> italicChars = CharacterCollector.getCharacters(FontStyle.ITALIC);

        assertTrue(plainChars.contains('P'));
        assertTrue(boldChars.contains('B'));
        assertTrue(italicChars.contains('I'));
    }

    @Test
    public void testCharacterRegistrationWithCustomDelimiter() {
        CharacterCollector.reset();
        Label label = new Label("Plain|Bold|Italic");
        label.setStyles(new FontStyle[]{FontStyle.PLAIN, FontStyle.BOLD, FontStyle.ITALIC}, "|");

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);
        Set<Character> italicChars = CharacterCollector.getCharacters(FontStyle.ITALIC);

        assertTrue(plainChars.contains('P'));
        assertTrue(plainChars.contains('|')); // First delimiter registered with PLAIN
        assertTrue(boldChars.contains('B'));
        assertTrue(boldChars.contains('|')); // Second delimiter registered with BOLD
        assertTrue(italicChars.contains('I'));
    }

    @Test
    public void testCharacterRegistrationWithEmptySegments() {
        CharacterCollector.reset();
        Label label = new Label("Text||More");
        label.setStyles(new FontStyle[]{FontStyle.PLAIN, FontStyle.BOLD, FontStyle.ITALIC}, "|");

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertTrue(plainChars.contains('T'));
        assertTrue(plainChars.contains('e'));
        // Empty segment should be skipped
        Set<Character> italicChars = CharacterCollector.getCharacters(FontStyle.ITALIC);
        assertTrue(italicChars.contains('M'));
    }

    @Test
    public void testCharacterRegistrationWithFewerStylesThanSegments() {
        CharacterCollector.reset();
        Label label = new Label("One|Two|Three|Four");
        label.setStyles(new FontStyle[]{FontStyle.PLAIN, FontStyle.BOLD}, "|");

        // Extra segments should use the last style (BOLD)
        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);

        assertTrue(plainChars.contains('O'));
        assertTrue(boldChars.contains('T'));
        assertTrue(boldChars.contains('F')); // "Four" should use BOLD (last style)
    }

    @Test
    public void testStyleChangeReregistersCharacters() {
        CharacterCollector.reset();
        Label label = new Label("Test");
        label.setStyle(FontStyle.PLAIN);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertTrue(plainChars.contains('T'));

        // Change to bold - should re-register
        label.setStyle(FontStyle.BOLD);

        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);
        assertTrue(boldChars.contains('T'));
        assertTrue(boldChars.contains('e'));
    }
}
