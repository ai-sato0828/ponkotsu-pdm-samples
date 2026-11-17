package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class WeekYearParsingExample {
    // BEGIN formatter
    private static final DateTimeFormatter WRONG_CALENDAR_DATE =
            DateTimeFormatter.ofPattern("YYYY/MM/dd", Locale.JAPAN)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter CORRECT_CALENDAR_DATE =
            DateTimeFormatter.ofPattern("uuuu/MM/dd", Locale.JAPAN)
                    .withResolverStyle(ResolverStyle.STRICT);
    // END formatter

    private WeekYearParsingExample() {}

    // BEGIN comparison
    public static LocalDate parseWrongWeekYear(String value) {
        return LocalDate.parse(value, WRONG_CALENDAR_DATE);
    }

    public static LocalDate parseCorrectCalendarDate(String value) {
        return LocalDate.parse(value, CORRECT_CALENDAR_DATE);
    }
    // END comparison

    // BEGIN main
    public static void main(String[] args) {
        for (String input : new String[] {"2026/06/15", "2026/12/31", "2027/01/01"}) {
            try {
                System.out.printf("YYYY %s -> %s%n", input, parseWrongWeekYear(input));
            } catch (DateTimeParseException failure) {
                System.out.printf("YYYY %s -> DateTimeParseException%n", input);
            }
            System.out.printf("uuuu %s -> %s%n", input, parseCorrectCalendarDate(input));
        }
    }
    // END main
}
