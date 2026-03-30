package io.quarkus.infra.performance.graphics.charts.fonts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCollectorTest {

    @BeforeEach
    void setUp() {
        CharacterCollector.reset();
    }

    @AfterEach
    void tearDown() {
        CharacterCollector.reset();
    }

    @Test
    void testRegisterTextWithStyle() {
        CharacterCollector.registerText("abc", FontStyle.PLAIN);
        CharacterCollector.registerText("123", FontStyle.BOLD);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);

        assertEquals(3, plainChars.size());
        assertTrue(plainChars.contains('a'));
        assertTrue(plainChars.contains('b'));
        assertTrue(plainChars.contains('c'));

        assertEquals(3, boldChars.size());
        assertTrue(boldChars.contains('1'));
        assertTrue(boldChars.contains('2'));
        assertTrue(boldChars.contains('3'));
    }

    @Test
    void testRegisterTextDefaultStyle() {
        CharacterCollector.registerText("hello");

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);

        assertEquals(4, plainChars.size()); // h, e, l, o (l appears twice)
        assertTrue(plainChars.contains('h'));
        assertTrue(plainChars.contains('e'));
        assertTrue(plainChars.contains('l'));
        assertTrue(plainChars.contains('o'));
    }


    @Test
    void testRegisterTextArray() {
        CharacterCollector.registerText(new String[]{"hello", "world"}, FontStyle.BOLD);

        Set<Character> boldChars = CharacterCollector.getCharacters(FontStyle.BOLD);

        assertTrue(boldChars.contains('h'));
        assertTrue(boldChars.contains('w'));
        assertTrue(boldChars.contains('d'));
    }

    @Test
    void testRegisterTextArrayWithPlainStyle() {
        CharacterCollector.registerText(new String[]{"foo", "bar", "baz"}, FontStyle.PLAIN);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);

        assertTrue(plainChars.contains('f'));
        assertTrue(plainChars.contains('b'));
        assertTrue(plainChars.contains('a'));
        assertTrue(plainChars.contains('z'));
    }

    @Test
    void testReset() {
        CharacterCollector.registerText("abc", FontStyle.PLAIN);
        assertEquals(3, CharacterCollector.getCharacters(FontStyle.PLAIN).size());

        CharacterCollector.reset();

        assertEquals(0, CharacterCollector.getCharacters(FontStyle.PLAIN).size());
    }

    @Test
    void testNullHandling() {
        // Should not throw exceptions
        CharacterCollector.registerText((String) null, FontStyle.PLAIN);
        CharacterCollector.registerText("text", null);
        CharacterCollector.registerText((String) null);
        CharacterCollector.registerText((String[]) null, FontStyle.PLAIN);

        // Verify no characters were collected
        assertEquals(0, CharacterCollector.getCharacters(FontStyle.PLAIN).size());
    }

    @Test
    void testEmptyStringHandling() {
        CharacterCollector.registerText("", FontStyle.PLAIN);
        CharacterCollector.registerText("abc", FontStyle.PLAIN);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertEquals(3, plainChars.size());
    }

    @Test
    void testDuplicateCharacters() {
        CharacterCollector.registerText("aaa", FontStyle.PLAIN);
        CharacterCollector.registerText("aaa", FontStyle.PLAIN);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);
        assertEquals(1, plainChars.size());
        assertTrue(plainChars.contains('a'));
    }

    @Test
    void testSameCharacterDifferentStyles() {
        CharacterCollector.registerText("a", FontStyle.PLAIN);
        CharacterCollector.registerText("a", FontStyle.BOLD);
        CharacterCollector.registerText("a", FontStyle.ITALIC);

        assertEquals(1, CharacterCollector.getCharacters(FontStyle.PLAIN).size());
        assertEquals(1, CharacterCollector.getCharacters(FontStyle.BOLD).size());
        assertEquals(1, CharacterCollector.getCharacters(FontStyle.ITALIC).size());
    }

    @Test
    void testSpecialCharacters() {
        CharacterCollector.registerText("Hello, World! 123 μ%", FontStyle.PLAIN);

        Set<Character> plainChars = CharacterCollector.getCharacters(FontStyle.PLAIN);

        assertTrue(plainChars.contains(','));
        assertTrue(plainChars.contains('!'));
        assertTrue(plainChars.contains(' '));
        assertTrue(plainChars.contains('μ'));
        assertTrue(plainChars.contains('%'));
    }

    @Test
    void testGetCharactersReturnsNewSet() {
        CharacterCollector.registerText("abc", FontStyle.PLAIN);

        Set<Character> chars1 = CharacterCollector.getCharacters(FontStyle.PLAIN);
        Set<Character> chars2 = CharacterCollector.getCharacters(FontStyle.PLAIN);

        assertNotSame(chars1, chars2);
        assertEquals(chars1, chars2);

        // Modifying the returned set should not affect the collector
        chars1.clear();
        assertEquals(3, CharacterCollector.getCharacters(FontStyle.PLAIN).size());
    }
}
