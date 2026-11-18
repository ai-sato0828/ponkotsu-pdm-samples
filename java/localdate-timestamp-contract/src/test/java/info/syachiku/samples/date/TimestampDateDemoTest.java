package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimestampDateDemoTest {
    private static final String LOCAL_TIMESTAMP = "2026-02-28T23:30:00";
    private static final String OFFSET_TIMESTAMP = "2026-02-28T23:30:00-05:00";

    @Test
    void dateOnlyParserReturnsTheDate() {
        assertEquals(LocalDate.of(2026, 2, 28), TimestampDateDemo.parseDateOnly("2026-02-28"));
    }

    @Test
    void dateOnlyParserStopsAtTheTimestampSeparator() {
        DateTimeParseException error = assertThrows(DateTimeParseException.class,
                () -> TimestampDateDemo.parseDateOnly(LOCAL_TIMESTAMP));
        assertEquals(10, error.getErrorIndex());
        assertEquals('T', LOCAL_TIMESTAMP.charAt(error.getErrorIndex()));
    }

    @Test
    void dateOnlyParserRejectsANonexistentDate() {
        assertThrows(DateTimeParseException.class,
                () -> TimestampDateDemo.parseDateOnly("2026-02-30"));
    }

    @Test
    void localTimestampParserReturnsItsLocalDate() {
        assertEquals(LocalDate.of(2026, 2, 28), TimestampDateDemo.parseLocalTimestampDate(LOCAL_TIMESTAMP));
    }

    @Test
    void datePrefixCanHideAnInvalidTime() {
        String invalidTime = "2026-02-28T25:30:00";
        assertEquals(LocalDate.of(2026, 2, 28), TimestampDateDemo.parseDateOnly(invalidTime.substring(0, 10)));
        assertThrows(DateTimeParseException.class, () -> TimestampDateDemo.parseLocalTimestampDate(invalidTime));
    }

    @Test
    void localTimestampParserStopsWhereTheOffsetStarts() {
        DateTimeParseException error = assertThrows(DateTimeParseException.class,
                () -> TimestampDateDemo.parseLocalTimestampDate(OFFSET_TIMESTAMP));
        assertEquals(19, error.getErrorIndex());
        assertEquals('-', OFFSET_TIMESTAMP.charAt(error.getErrorIndex()));
    }

    @Test
    void sourceOffsetDateIsStillFebruary28() {
        assertEquals(LocalDate.of(2026, 2, 28), TimestampDateDemo.dateAtSourceOffset(OFFSET_TIMESTAMP));
    }

    @Test
    void theSameInstantIsMarch1InTokyo() {
        assertEquals(LocalDate.of(2026, 3, 1), TimestampDateDemo.dateInZone(OFFSET_TIMESTAMP, ZoneId.of("Asia/Tokyo")));
    }
}
