package io.quarkus.infra.performance.graphics.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnownFrameworkTest {
    @Test
    void getExpandedNameForSimpleQuarkusCase() {
        assertEquals("Quarkus\nJIT (via OpenJDK)", KnownFramework.QUARKUS3_JVM.getExpandedName());
    }

    @Test
    void getExpandedNameForSimpleSpringCase() {
        assertEquals("Spring Boot 3\nJIT (via OpenJDK)", KnownFramework.SPRING3_JVM.getExpandedName());
    }

    @Test
    void getCategories() {
        assertTrue(KnownFramework.QUARKUS3_JVM.hasCategory(Category.QUARKUS));
        assertFalse(KnownFramework.SPRING3_JVM.hasCategory(Category.QUARKUS));
    }

    @Test
    void isInGroupItIsIn() {
        assertTrue(KnownFramework.QUARKUS3_VIRTUAL.isInGroup(Group.QUARKUS));
    }

    @Test
    void isAlwaysInAllGroup() {
        assertTrue(KnownFramework.QUARKUS3_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING3_NATIVE.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING_NATIVE.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING3_JVM_AOT.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING4_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(KnownFramework.QUARKUS3_JVM.isInGroup(Group.ALL));
    }

    @Test
    void isInGroupItIsNotIn() {
        assertFalse(KnownFramework.SPRING3_JVM.isInGroup(Group.QUARKUS));
    }

    @Test
    void partitionableCategory() {
        assertEquals(KnownFramework.SPRING3_JVM.getPartitionableCategory(), Category.VANILLA_JIT);
        assertEquals(KnownFramework.SPRING3_NATIVE.getPartitionableCategory(), Category.NATIVE);
        assertEquals(KnownFramework.SPRING3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
        assertEquals(KnownFramework.QUARKUS3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
    }

}
