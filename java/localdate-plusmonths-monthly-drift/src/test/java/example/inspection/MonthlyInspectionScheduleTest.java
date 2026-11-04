package example.inspection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyInspectionScheduleTest {
    private final MonthlyInspectionSchedule day31 = new MonthlyInspectionSchedule(31);

    @Test
    void rollingCalculationLosesThe31stAfterFebruary() {
        assertEquals(List.of(
                        LocalDate.of(2026, 1, 31),
                        LocalDate.of(2026, 2, 28),
                        LocalDate.of(2026, 3, 28)),
                InspectionScheduleDemo.rollingDates(LocalDate.of(2026, 1, 31), 3));
    }

    @Test
    void returnsToThe31stAfterACommonFebruary() {
        assertEquals(List.of(
                        LocalDate.of(2026, 1, 31),
                        LocalDate.of(2026, 2, 28),
                        LocalDate.of(2026, 3, 31)),
                day31.datesFrom(YearMonth.of(2026, 1), 3));
    }

    @Test
    void returnsToThe31stWhenScheduleStartsInALeapFebruary() {
        assertEquals(List.of(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 31)),
                day31.datesFrom(YearMonth.of(2028, 2), 2));
    }

    @Test
    void fixedDay30AndMonthEndDifferInMarch() {
        var day30 = new MonthlyInspectionSchedule(30);
        YearMonth february = YearMonth.of(2026, 2);
        YearMonth march = YearMonth.of(2026, 3);

        assertEquals(february.atEndOfMonth(), day30.dateIn(february));
        assertEquals(LocalDate.of(2026, 3, 30), day30.dateIn(march));
        assertEquals(LocalDate.of(2026, 3, 31), march.atEndOfMonth());
    }

    @Test
    void crossesYearBoundaryWithoutLosingTheDay() {
        assertEquals(List.of(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 31)),
                day31.datesFrom(YearMonth.of(2026, 12), 2));
    }

    @Test
    void zeroMonthsReturnsNoDates() {
        assertEquals(List.of(), day31.datesFrom(YearMonth.of(2026, 1), 0));
    }

    @Test
    void rejectsNegativeMonthCount() {
        assertThrows(IllegalArgumentException.class,
                () -> day31.datesFrom(YearMonth.of(2026, 1), -1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    void rejectsAnInvalidAnchorDay(int dayOfMonth) {
        assertThrows(IllegalArgumentException.class, () -> new MonthlyInspectionSchedule(dayOfMonth));
    }
}
