package io.quarkus.infra.performance.graphics.util;

public class StringHelpers {
    public static String prettify(String name) {
        // Convert kebab-case or snake_case to a more readable format
        String withSpaces = name.replace("-", " ").replace("_", " ");
        // Capitalize first letter of each word
        String[] words = withSpaces.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (! word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        String formatted = result.toString();

        String sanitised = formatted.replace("Aot", "AOT").replace("Jit", "JVM").replace("Vanilla", "");
        String trimmed = sanitised.trim();
        if (trimmed.isEmpty()) {
            // If we got rid of all the vanillas, put back something
            return "Default";
        } else {
            return trimmed;
        }
    }
}
