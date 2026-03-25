package io.quarkus.infra.performance.graphics.model;

import java.util.Comparator;
import java.util.Set;

/**
 * Represents a framework type, either known (enum-based) or unknown (dynamically created).
 */
public interface Framework {

    /**
     * Comparator that ensures:
     * 1. Known frameworks come first, sorted by enum ordinal
     * 2. Unknown frameworks come after, in insertion order
     */
    Comparator<Framework> COMPARATOR = (f1, f2) -> {
        // Both are KnownFramework enums - compare by ordinal
        if (f1 instanceof KnownFramework && f2 instanceof KnownFramework) {
            return ((KnownFramework) f1).compareTo((KnownFramework) f2);
        }
        // f1 is KnownFramework, f2 is unknown - KnownFramework comes first
        if (f1 instanceof KnownFramework) {
            return - 1;
        }
        // f2 is KnownFramework, f1 is unknown - KnownFramework comes first
        if (f2 instanceof KnownFramework) {
            return 1;
        }
        // Both unknown - maintain insertion order (already in LinkedHashMap order)
        return 0;
    };

    public static Framework valueOfIgnoreCase(String key) {
        Framework framework = KnownFramework.valueOfIgnoreCase(key);
        if (framework == null) {
            // Create an unknown framework instance
            framework = new UnknownFramework(key);
        }
        return framework;
    }

    /**
     * Get the internal name/key of the framework
     */
    String getName();

    /**
     * Get the expanded display name of the framework
     */
    String getExpandedName();

    /**
     * Check if this framework belongs to a specific group
     */
    boolean isInGroup(Group group);

    /**
     * Check if this framework has a specific category
     */
    boolean hasCategory(Category category);

    /**
     * Get the partitionable category for this framework
     */
    Category getPartitionableCategory();

    /**
     * Get all categories for this framework
     */
    Set<Category> getCategories();
}

// Made with Bob
