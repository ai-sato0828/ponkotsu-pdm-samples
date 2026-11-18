package example.importcode;

import java.util.Objects;

public final class FixedWidthCode {
    private FixedWidthCode() {}

    // BEGIN before
    public static String extractUnchecked(String input) {
        Objects.requireNonNull(input, "input");
        return input.substring(2, 6);
    }
    // END before

    // BEGIN after
    public static String extractPayload(String input) {
        Objects.requireNonNull(input, "input");
        if (input.length() < 6) {
            throw new IllegalArgumentException(
                    "取込コードは6文字以上が必要です: 実際=" + input.length());
        }
        return input.substring(2, 6);
    }
    // END after
}
