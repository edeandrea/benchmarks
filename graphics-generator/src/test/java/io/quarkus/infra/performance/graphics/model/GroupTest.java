package io.quarkus.infra.performance.graphics.model;

import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING4_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_VIRTUAL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupTest {

    @Test
    public void allIncludesEverything() {
        Group group = Group.ALL;
        assertNotNull(group);
        // This isn't an exhaustive test, but it's indicative
        assertTrue(group.contains(SPRING4_VIRTUAL));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM_AOT));
    }

    // A group constructed by hand
    @Test
    public void mainComparisonHasRightEntries() {
        Group group = Group.MAIN_COMPARISON;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM));
        assertTrue(group.contains(SPRING4_NATIVE));
        assertFalse(group.contains(QUARKUS3_SPRING4_COMPAT));
        assertFalse(group.contains(QUARKUS3_VIRTUAL));
    }

    // A group constructed using a category
    @Test
    public void quarkusIncludesQuarkusButNotSpring() {
        Group group = Group.QUARKUS;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertFalse(group.contains(SPRING4_JVM));
    }

    // A group using an exclusion
    // A group constructed using a category
    @Test
    public void javaFrameworksExcludesOlderSpringVersions() {
        Group group = Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM));
        assertTrue(group.contains(SPRING4_LEYDEN));
        assertTrue(group.contains(SPRING4_JVM_AOT));
        assertFalse(group.contains(SPRING4_VIRTUAL));
        assertFalse(group.contains(SPRING3_JVM));
        assertFalse(group.contains(SPRING3_JVM_AOT));
    }

    @Test
    public void categoryChecks() {
        Group group = Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS;
        assertTrue(group.containsAny(Category.QUARKUS));
        assertTrue(group.containsAny(Category.JVM));
        assertTrue(group.containsAny(Category.NATIVE));
        assertFalse(group.containsAny(Category.VIRTUAL_THREADS));


        group = Group.QUARKUS;
        assertTrue(group.containsAny(Category.QUARKUS));
        assertTrue(group.containsAny(Category.JVM));
        assertTrue(group.containsAny(Category.NATIVE));
        assertFalse(group.containsAny(Category.SPRING));

    }
}
