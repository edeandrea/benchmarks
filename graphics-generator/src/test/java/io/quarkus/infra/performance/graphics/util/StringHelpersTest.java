package io.quarkus.infra.performance.graphics.util;

import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.util.StringHelpers.prettify;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringHelpersTest {

    @Test
    public void singleWord() {
        assertEquals("Hello", prettify("HELLO"));
    }

    @Test
    public void singleWordLowerCase() {
        assertEquals("Hello", prettify("hello"));
    }

    @Test
    public void phrases() {
        assertEquals("Hello World", prettify("HELLO_WORLD"));
        assertEquals("Hello World", prettify("HELLO-WORLD"));
    }

    @Test
    public void specialCasingKnownAcronyms() {
        // Make sure some words stay upper case
        assertEquals("Hello AOT", prettify("HELLO_AOT"));
        assertEquals("Hello JVM", prettify("HELLO_JIT"));
    }

    @Test
    public void specialCasingInternalTerminology() {
        assertEquals("Hello", prettify("HELLO_VANILLA"));
        assertEquals("Hello", prettify("VANILLA_HELLO"));
    }

    @Test
    public void specialCasingInternalTerminologyNeverCreatesEmptyStrings() {
        assertEquals("Default", prettify("VANILLA"));
        assertEquals("Default", prettify("VANILLA "));
    }

}