package info.syachiku.calendar;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessDaysTest {
    private static final LocalDate FRIDAY = LocalDate.of(2026, 11, 13);
    private static final LocalDate MONDAY_CLOSURE = LocalDate.of(2026, 11, 16);

    @Test
    void skipsWeekendAndCompanyClosure() {
        assertEquals(LocalDate.of(2026, 11, 17),
                BusinessDays.add(FRIDAY, 1, Set.of(MONDAY_CLOSURE)));
    }

    @Test
    void countsTwoOpenDays() {
        assertEquals(LocalDate.of(2026, 11, 18),
                BusinessDays.add(FRIDAY, 2, Set.of(MONDAY_CLOSURE)));
    }

    @Test
    void startsWithTheFollowingDay() {
        assertEquals(FRIDAY,
                BusinessDays.add(LocalDate.of(2026, 11, 12), 1, Set.of()));
    }

    @Test
    void leavesClosedStartForZero() {
        assertEquals(MONDAY_CLOSURE,
                BusinessDays.add(MONDAY_CLOSURE, 0, Set.of(MONDAY_CLOSURE)));
    }

    @Test
    void crossesYearWithProvidedClosures() {
        var closures = Set.of(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4));
        assertEquals(LocalDate.of(2027, 1, 5),
                BusinessDays.add(LocalDate.of(2026, 12, 31), 1, closures));
    }

    @Test
    void doesNotShiftForAnExtraClosedWeekendDate() {
        var closures = Set.of(LocalDate.of(2026, 11, 14), MONDAY_CLOSURE);
        assertEquals(LocalDate.of(2026, 11, 17),
                BusinessDays.add(FRIDAY, 1, closures));
    }

    @Test
    void allowsSixtyBusinessDays() {
        assertEquals(LocalDate.of(2027, 2, 8),
                BusinessDays.add(LocalDate.of(2026, 11, 16), 60, Set.of()));
    }

    @Test
    void rejectsNegativeDays() {
        assertThrows(IllegalArgumentException.class,
                () -> BusinessDays.add(FRIDAY, -1, Set.of()));
    }

    @Test
    void rejectsMoreThanSixty() {
        assertThrows(IllegalArgumentException.class,
                () -> BusinessDays.add(FRIDAY, 61, Set.of()));
    }

    @Test
    void failsWhenResultExceedsLocalDateMax() {
        assertThrows(DateTimeException.class,
                () -> BusinessDays.add(LocalDate.MAX, 1, Set.of()));
    }
}
