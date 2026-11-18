package example.date;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class MonthNameDateDemoTest {
    @Test
    void japaneseLocaleCannotReadEnglishMonthName() {
        String input = "18 Nov 2026";
        var failure = assertThrowsExactly(DateTimeParseException.class,
                () -> MonthNameDateDemo.parse(input, Locale.JAPAN));
        assertEquals(3, failure.getErrorIndex());
    }

    @Test
    void englishLocaleReadsTheSameInputAsALocalDate() {
        assertEquals(LocalDate.of(2026, 11, 18),
                MonthNameDateDemo.parse("18 Nov 2026", Locale.ENGLISH));
    }

    @Test
    void englishLocaleWithStrictStillRejectsFebruary30() {
        assertThrowsExactly(DateTimeParseException.class,
                () -> MonthNameDateDemo.parse("30 Feb 2026", Locale.ENGLISH));
    }
}
