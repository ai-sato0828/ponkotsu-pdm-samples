package example.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class MonthNameDateDemo {
    private static final String PATTERN = "dd MMM uuuu";

    public static void main(String[] args) {
        printResult("18 Nov 2026", Locale.JAPAN);
        printResult("18 Nov 2026", Locale.ENGLISH);
        printResult("30 Feb 2026", Locale.ENGLISH);
    }

    static LocalDate parse(String input, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN, locale)
                .withResolverStyle(ResolverStyle.STRICT);
        return LocalDate.parse(input, formatter);
    }

    private static void printResult(String input, Locale locale) {
        String label = "locale=" + locale.toLanguageTag() + " input=\"" + input + "\"";
        try {
            System.out.println(label + " -> " + parse(input, locale));
        } catch (DateTimeParseException failure) {
            System.out.println(label + " -> DateTimeParseException (index=" + failure.getErrorIndex() + ")");
            System.out.println("  " + failure.getMessage());
        }
    }
}
