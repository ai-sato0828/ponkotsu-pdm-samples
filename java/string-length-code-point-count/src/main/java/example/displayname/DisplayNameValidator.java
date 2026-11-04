package example.displayname;

public final class DisplayNameValidator {
    private static final int MAX_CODE_POINTS = 3;

    private DisplayNameValidator() {
    }

    public static boolean accepts(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.codePointCount(0, name.length()) <= MAX_CODE_POINTS;
    }
}
