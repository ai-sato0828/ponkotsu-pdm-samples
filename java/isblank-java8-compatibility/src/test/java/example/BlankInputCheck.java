package example;

import java.util.function.Predicate;

public final class BlankInputCheck {
    private static final Case[] CASES = {
        new Case("null", null, true, true),
        new Case("empty", "", true, true),
        new Case("ASCII spaces", "   ", true, true),
        new Case("tab and newline", "\t\n", true, true),
        new Case("full-width U+3000", "\u3000", true, false),
        new Case("NBSP U+00A0", "\u00A0", false, false),
        new Case("ordinary text", "sample", false, false)
    };

    private BlankInputCheck() {
    }

    public static void main(String[] args) {
        verifyAgainst(null);
    }

    static void verifyAgainst(Predicate<String> java11) {
        System.out.println("input | Java 8-compatible | String.isBlank | trim().isEmpty");
        for (Case current : CASES) {
            boolean compatible = BlankInput8.isMissing(current.value);
            boolean trimmed = current.value == null || current.value.trim().isEmpty();

            requireExpected(current, "Java 8-compatible", current.expected, compatible);
            requireExpected(current, "trim().isEmpty", current.expectedTrim, trimmed);

            String reference = "-";
            if (java11 != null) {
                boolean modern = java11.test(current.value);
                requireExpected(current, "String.isBlank", current.expected, modern);
                reference = Boolean.toString(modern);
            }

            System.out.println(current.label + " | " + compatible + " | " + reference + " | " + trimmed);
        }
        System.out.println("Verified " + CASES.length + " cases.");
    }

    private static void requireExpected(Case current, String method, boolean expected, boolean actual) {
        if (actual != expected) {
            throw new AssertionError(current.label + " (" + method + "): expected " + expected + ", got " + actual);
        }
    }

    private static final class Case {
        private final String label;
        private final String value;
        private final boolean expected;
        private final boolean expectedTrim;

        private Case(String label, String value, boolean expected, boolean expectedTrim) {
            this.label = label;
            this.value = value;
            this.expected = expected;
            this.expectedTrim = expectedTrim;
        }
    }
}
