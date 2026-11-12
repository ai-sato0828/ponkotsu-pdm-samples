package info.syachiku.split;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SplitFields {

    private SplitFields() {
    }

    // 比較専用の誤った例。ピリオドそのものを区切っていない。
    public static String[] wrongDot(String input, int limit) {
        return Objects.requireNonNull(input, "input").split(".", limit);
    }

    public static String[] literalDot(String input, int limit) {
        return Objects.requireNonNull(input, "input").split("\\.", limit);
    }

    public static String[] literalDelimiter(String input, String delimiter, int limit) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(delimiter, "delimiter");
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("delimiter must not be empty");
        }
        return input.split(Pattern.quote(delimiter), limit);
    }

    public static String describe(String[] values) {
        Objects.requireNonNull(values, "values");
        String contents = Arrays.stream(values)
                .map(value -> "\"" + value.replace("\\", "\\\\")
                        .replace("\"", "\\\"").replace("\n", "\\n") + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
        return "length=" + values.length + " values=" + contents;
    }
}
