package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OffsetTimestampDateTest {
    private static final String UTC = "2026-02-28T23:30:00Z";
    private static final String PLUS_NINE = "2026-03-01T08:30:00+09:00";

    @ParameterizedTest
    @CsvSource({UTC + ", Z", PLUS_NINE + ", +"})
    void defaultLocalDateTimeParserStopsAtTheOffset(String input, String offsetStart) {
        DateTimeParseException failure = assertThrows(DateTimeParseException.class,
                () -> LocalDateTime.parse(input));

        assertEquals(input, failure.getParsedString());
        assertEquals(19, failure.getErrorIndex());
        assertEquals(offsetStart.charAt(0), input.charAt(failure.getErrorIndex()));
    }

    @ParameterizedTest
    @CsvSource({UTC + ", 2026-02-28", PLUS_NINE + ", 2026-03-01"})
    void localDateKeepsTheDateWrittenWithTheInputOffset(String input, String expectedDate) {
        assertEquals(LocalDate.parse(expectedDate), OffsetDateTime.parse(input).toLocalDate());
    }

    @Test
    void twoOffsetRepresentationsHaveTheSameInstantAndTokyoDate() {
        OffsetDateTime utc = OffsetDateTime.parse(UTC);
        OffsetDateTime plusNine = OffsetDateTime.parse(PLUS_NINE);

        assertEquals(Instant.parse(UTC), utc.toInstant());
        assertEquals(utc.toInstant(), plusNine.toInstant());
        assertEquals(LocalDate.of(2026, 3, 1),
                OffsetTimestampDateDemo.dateInZone(utc, OffsetTimestampDateDemo.TOKYO));
        assertEquals(LocalDate.of(2026, 3, 1),
                OffsetTimestampDateDemo.dateInZone(plusNine, OffsetTimestampDateDemo.TOKYO));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-02-28T14:59:59Z, 2026-02-28",
            "2026-02-28T15:00:00Z, 2026-03-01"
    })
    void tokyoDateChangesAtMidnightInTokyo(String input, String expectedDate) {
        assertEquals(LocalDate.parse(expectedDate), OffsetTimestampDateDemo.dateInZone(
                OffsetDateTime.parse(input), OffsetTimestampDateDemo.TOKYO));
    }
}
