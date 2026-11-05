package example.dates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyDateRunnerTest {
    @Test
    void runsSameDateOnce() {
        LocalDate date = LocalDate.of(2026, 1, 31);
        List<LocalDate> visited = new ArrayList<>();
        DailyDateRunner.runInclusive(date, date, visited::add);
        assertEquals(List.of(date), visited);
    }

    @Test
    void runsThroughLeapDayInOrder() {
        LocalDate from = LocalDate.of(2024, 2, 28);
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        LocalDate to = LocalDate.of(2024, 3, 1);
        List<LocalDate> visited = new ArrayList<>();

        DailyDateRunner.runInclusive(from, to, date -> {
            if (visited.contains(date)) {
                throw new AssertionError("同じ日を再処理しました " + date);
            }
            visited.add(date);
        });

        assertEquals(List.of(from, leapDay, to), visited);
    }

    @Test
    void rejectsReversedRangeBeforeProcessing() {
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        List<LocalDate> visited = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> DailyDateRunner.runInclusive(from, to, visited::add));
        assertTrue(visited.isEmpty());
    }

    @Test
    void stopsAtMaximumDateWithoutAddingADay() {
        List<LocalDate> visited = new ArrayList<>();
        DailyDateRunner.runInclusive(LocalDate.MAX, LocalDate.MAX, visited::add);
        assertEquals(List.of(LocalDate.MAX), visited);
    }
}
