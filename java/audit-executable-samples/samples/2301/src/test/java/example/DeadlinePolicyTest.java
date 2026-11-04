package example;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlinePolicyTest {
    private static final ZoneId JAPAN = ZoneId.of("Asia/Tokyo");

    @ParameterizedTest(name = "UTC={0}, 締切={1} → 申請可能={2}")
    @CsvSource({
        "2026-04-01T14:59:59Z, 2026-04-01, true",
        "2026-04-01T15:00:00Z, 2026-04-01, false",
        "2026-04-01T15:00:00Z, 2026-04-02, true"
    })
    void deadlineBoundary(String utcTime, String deadlineText, boolean expected) {
        Clock fixed = Clock.fixed(Instant.parse(utcTime), JAPAN);
        DeadlinePolicy policy = new DeadlinePolicy(fixed);

        boolean actual = policy.canSubmit(LocalDate.parse(deadlineText));

        assertEquals(expected, actual);
    }

    @Test
    void sameInstantCanBeDifferentCalendarDates() {
        Instant midnightInJapan = Instant.parse("2026-04-01T15:00:00Z");
        LocalDate deadline = LocalDate.parse("2026-04-01");

        assertEquals(false, new DeadlinePolicy(Clock.fixed(midnightInJapan, JAPAN)).canSubmit(deadline));
        assertEquals(true, new DeadlinePolicy(Clock.fixed(midnightInJapan, ZoneOffset.UTC)).canSubmit(deadline));
    }
}
