package io.quarkus.infra.performance.graphics.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static io.quarkus.infra.performance.graphics.util.StringHelpers.prettify;

/**
 * Represents a framework that is not known at compile time.
 * Unknown frameworks are created dynamically when parsing JSON data.
 */
public class UnknownFramework implements Framework {

    private final String name;
    private final String expandedName;

    public UnknownFramework(String name) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.expandedName = formatExpandedName(name);
    }

    private static String formatExpandedName(String name) {
        String result = prettify(name);
        return result + "\n(No details)";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExpandedName() {
        return expandedName;
    }

    @Override
    public boolean isInGroup(Group group) {
        // Unknown frameworks are not in any specific group except ALL
        return group == Group.ALL;
    }

    @Override
    public boolean hasCategory(Category category) {
        // Unknown frameworks don't have categories
        return false;
    }

    @Override
    public Category getPartitionableCategory() {
        // Unknown frameworks don't have a partitionable category
        return null;
    }

    @Override
    public Set<Category> getCategories() {
        return EnumSet.noneOf(Category.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownFramework that = (UnknownFramework) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "UnknownFramework{" + name + "}";
    }
}

// Made with Bob
