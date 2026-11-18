package example;

public final class BlankInput8 {
    private BlankInput8() {
    }

    public static boolean isMissing(String value) {
        return value == null || value.codePoints().allMatch(Character::isWhitespace);
    }
}
