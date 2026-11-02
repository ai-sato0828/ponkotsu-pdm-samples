package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateInputsTest {
    @Test
    void defaultSmartAdjustsInvalidDateButOurParserRejectsIt() {
        var smart = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        assertEquals(ResolverStyle.SMART, smart.getResolverStyle());
        assertEquals(LocalDate.of(2026, 2, 28),
                LocalDate.parse("2026/02/30", smart));
        assertThrows(DateTimeParseException.class,
                () -> DateInputs.parseSlashDate("2026/02/30"));
    }

    @ParameterizedTest
    @CsvSource({"2026/08/14,2026-08-14", "2024/02/29,2024-02-29",
            "0000/01/01,0000-01-01"})
    void requiredAcceptsExistingDates(String input, String expected) {
        assertEquals(LocalDate.parse(expected), DateInputs.parseSlashDate(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026/02/30", "2026/02/29", "2026/13/01",
            "2026/00/01", "2026/04/31", "2026-02-28", "2026/2/28",
            " 2026/02/28 ", "２０２６/02/28", "20260228"})
    void requiredRejectsInvalidShapeOrDate(String input) {
        assertThrows(DateTimeParseException.class,
                () -> DateInputs.parseSlashDate(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "　"})
    void requiredRejectsMissingInput(String input) {
        var error = assertThrows(IllegalArgumentException.class,
                () -> DateInputs.parseSlashDate(input));
        assertEquals("日付は必須です", error.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "　"})
    void optionalReturnsEmptyOnlyForMissingInput(String input) {
        assertEquals(Optional.empty(), DateInputs.parseOptionalDate(input));
    }

    @Test
    void optionalReturnsExistingDate() {
        assertEquals(Optional.of(LocalDate.of(2024, 2, 29)),
                DateInputs.parseOptionalDate("2024/02/29"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026/02/30", "2026-02-28", "2026/2/28"})
    void optionalDoesNotHideInvalidInput(String input) {
        assertThrows(DateTimeParseException.class,
                () -> DateInputs.parseOptionalDate(input));
    }

    @Test
    void strictYearOfEraNeedsMoreInformationThanProlepticYear() {
        var strictYearOfEra = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withResolverStyle(ResolverStyle.STRICT);
        assertThrows(DateTimeParseException.class,
                () -> LocalDate.parse("2026/08/14", strictYearOfEra));
        assertEquals(LocalDate.of(2026, 8, 14),
                DateInputs.parseSlashDate("2026/08/14"));
    }
}
