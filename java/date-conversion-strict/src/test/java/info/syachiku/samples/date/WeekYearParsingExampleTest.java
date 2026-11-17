package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeekYearParsingExampleTest {
    @ParameterizedTest
    @CsvSource({
            "2026/06/15, 2026-06-15",
            "2026/12/31, 2026-12-31",
            "2027/01/01, 2027-01-01"
    })
    void aWeekBasedYearCannotResolveAMonthAndDayCalendarDate(
            String input, String expected) {
        var failure = assertThrows(DateTimeParseException.class,
                () -> WeekYearParsingExample.parseWrongWeekYear(input));
        assertEquals(input, failure.getParsedString());
        assertEquals(LocalDate.parse(expected),
                WeekYearParsingExample.parseCorrectCalendarDate(input));
        assertEquals(DateInputs.parseSlashDate(input),
                WeekYearParsingExample.parseCorrectCalendarDate(input));
    }
}
