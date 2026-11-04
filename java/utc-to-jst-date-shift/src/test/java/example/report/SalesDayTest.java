package example.report;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesDayTest {
    @Test
    void attachingTokyoToTheUtcClockChangesTheInstantAndSalesDay() {
        Instant paidAt = Instant.parse("2026-12-31T15:00:00Z");

        ZonedDateTime wrong = BrokenSalesDay.inTokyo(paidAt);

        assertEquals(LocalDate.parse("2026-12-31"), wrong.toLocalDate());
        assertEquals(Instant.parse("2026-12-31T06:00:00Z"), wrong.toInstant());
        assertEquals(LocalDate.parse("2027-01-01"), SalesDay.forTokyo(paidAt));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-12-31T14:59:59Z, 2026-12-31T23:59:59+09:00[Asia/Tokyo], 2026-12-31",
            "2026-12-31T15:00:00Z, 2027-01-01T00:00:00+09:00[Asia/Tokyo], 2027-01-01"
    })
    void usesTheTokyoDateOnBothSidesOfMidnight(String utc, String tokyo, String date) {
        Instant paidAt = Instant.parse(utc);

        assertEquals(ZonedDateTime.parse(tokyo), SalesDay.inTokyo(paidAt));
        assertEquals(LocalDate.parse(date), SalesDay.forTokyo(paidAt));
    }

    @Test
    void keepsTheSameInstantWhenTheDisplayZoneChanges() {
        Instant paidAt = Instant.parse("2026-12-31T15:00:00Z");

        assertEquals(paidAt, SalesDay.inTokyo(paidAt).toInstant());
    }

    @Test
    void doesNotAssumeAZoneForAnUnqualifiedTimestamp() {
        assertThrows(DateTimeParseException.class, () -> Instant.parse("2026-12-31T15:00:00"));
    }
}
