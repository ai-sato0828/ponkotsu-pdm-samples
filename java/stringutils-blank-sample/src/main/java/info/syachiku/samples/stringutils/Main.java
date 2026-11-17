package info.syachiku.samples.stringutils;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        List<Input> inputs = List.of(
                new Input("NULL", null),
                new Input("EMPTY", ""),
                new Input("HALF_SPACE", " "),
                new Input("FULL_SPACE", "\u3000"),
                new Input("NBSP", "\u00A0"),
                new Input("TAB_LF", "\t\n"),
                new Input("TEXT", "Alice"));

        for (Input input : inputs) {
            String value = input.value();
            String jdkEmpty = value == null ? "NPE" : Boolean.toString(value.isEmpty());
            String jdkBlank = value == null ? "NPE" : Boolean.toString(value.isBlank());
            System.out.printf(
                    "CASE=%s APACHE_EMPTY=%s APACHE_BLANK=%s JDK_EMPTY=%s JDK_BLANK=%s NAME_MISSING=%s MEMO=%s%n",
                    input.label(), StringUtils.isEmpty(value), StringUtils.isBlank(value),
                    jdkEmpty, jdkBlank, InputPolicy.isDisplayNameMissing(value),
                    visible(InputPolicy.memoOrDefault(value)));
        }
    }

    private static String visible(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\")
                .replace("\t", "\\t").replace("\n", "\\n")
                .replace(" ", "\\u0020").replace("\u3000", "\\u3000")
                .replace("\u00A0", "\\u00A0") + "\"";
    }

    private record Input(String label, String value) { }
}
