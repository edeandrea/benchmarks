package io.quarkus.infra.performance.graphics.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnknownFrameworkTest {

    @Test
    void testNameWithHyphens() {
        UnknownFramework framework = new UnknownFramework("unknown-framework-xyz");
        assertEquals("unknown-framework-xyz", framework.getName());
        assertEquals("Unknown Framework Xyz\n(No details)", framework.getExpandedName());
    }

    @Test
    void testNameWithUnderscores() {
        UnknownFramework framework = new UnknownFramework("my_custom_framework");
        assertEquals("my_custom_framework", framework.getName());
        assertEquals("My Custom Framework\n(No details)", framework.getExpandedName());
    }

    @Test
    void testNameWithMixedSeparators() {
        UnknownFramework framework = new UnknownFramework("test-framework_v2");
        assertEquals("test-framework_v2", framework.getName());
        assertEquals("Test Framework V2\n(No details)", framework.getExpandedName());
    }

    @Test
    void testSingleWordName() {
        UnknownFramework framework = new UnknownFramework("micronaut");
        assertEquals("micronaut", framework.getName());
        assertEquals("Micronaut\n(No details)", framework.getExpandedName());
    }

    @Test
    void testNameWithNumbers() {
        UnknownFramework framework = new UnknownFramework("framework-v3-beta");
        assertEquals("framework-v3-beta", framework.getName());
        assertEquals("Framework V3 Beta\n(No details)", framework.getExpandedName());
    }

    @Test
    void testHasNoCategories() {
        UnknownFramework framework = new UnknownFramework("test-framework");
        assertFalse(framework.hasCategory(Category.JVM));
        assertFalse(framework.hasCategory(Category.NATIVE));
        assertFalse(framework.hasCategory(Category.QUARKUS));
        assertFalse(framework.hasCategory(Category.SPRING));
        assertTrue(framework.getCategories().isEmpty());
    }

    @Test
    void testIsInAllGroup() {
        UnknownFramework framework = new UnknownFramework("test-framework");
        assertTrue(framework.isInGroup(Group.ALL));
    }

    @Test
    void testIsNotInSpecificGroups() {
        UnknownFramework framework = new UnknownFramework("test-framework");
        assertFalse(framework.isInGroup(Group.QUARKUS));
        assertFalse(framework.isInGroup(Group.MAIN_COMPARISON));
        assertFalse(framework.isInGroup(Group.JAVA_AND_NATIVE_FRAMEWORKS));
    }

    @Test
    void testNoPartitionableCategory() {
        UnknownFramework framework = new UnknownFramework("test-framework");
        assertNull(framework.getPartitionableCategory());
    }

    @Test
    void testEquality() {
        UnknownFramework framework1 = new UnknownFramework("test-framework");
        UnknownFramework framework2 = new UnknownFramework("test-framework");
        UnknownFramework framework3 = new UnknownFramework("other-framework");

        assertEquals(framework1, framework2);
        assertNotEquals(framework1, framework3);
        assertEquals(framework1.hashCode(), framework2.hashCode());
    }

    @Test
    void testToString() {
        UnknownFramework framework = new UnknownFramework("test-framework");
        assertEquals("UnknownFramework{test-framework}", framework.toString());
    }

    @Test
    void testNullNameThrowsException() {
        assertThrows(NullPointerException.class, () -> new UnknownFramework(null));
    }
}

// Made with Bob
