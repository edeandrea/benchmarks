package io.quarkus.infra.performance.graphics.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameworkTest {
    @Test
    void getExpandedNameForSimpleQuarkusCase() {
        assertEquals("Quarkus\nJIT (via OpenJDK)", Framework.QUARKUS3_JVM.getExpandedName());
    }

    @Test
    void getExpandedNameForSimpleSpringCase() {
        assertEquals("Spring Boot 3\nJIT (via OpenJDK)", Framework.SPRING3_JVM.getExpandedName());
    }

    @Test
    void getCategories() {
        assertTrue(Framework.QUARKUS3_JVM.hasCategory(Category.QUARKUS));
        assertFalse(Framework.SPRING3_JVM.hasCategory(Category.QUARKUS));
    }

    @Test
    void isInGroupItIsIn() {
        assertTrue(Framework.QUARKUS3_VIRTUAL.isInGroup(Group.QUARKUS));
    }

    @Test
    void isAlwaysInAllGroup() {
        assertTrue(Framework.QUARKUS3_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(Framework.SPRING3_NATIVE.isInGroup(Group.ALL));
        assertTrue(Framework.SPRING_NATIVE.isInGroup(Group.ALL));
        assertTrue(Framework.SPRING3_JVM_AOT.isInGroup(Group.ALL));
        assertTrue(Framework.SPRING4_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(Framework.QUARKUS3_JVM.isInGroup(Group.ALL));
    }

    @Test
    void isInGroupItIsNotIn() {
        assertFalse(Framework.SPRING3_JVM.isInGroup(Group.QUARKUS));
    }

    @Test
    void partitionableCategory() {
        assertEquals(Framework.SPRING3_JVM.getPartitionableCategory(), Category.VANILLA_JIT);
        assertEquals(Framework.SPRING3_NATIVE.getPartitionableCategory(), Category.NATIVE);
        assertEquals(Framework.SPRING3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
        assertEquals(Framework.QUARKUS3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
    }

}
