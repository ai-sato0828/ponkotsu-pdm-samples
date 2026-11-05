import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportSuccessRateTest {

    @ParameterizedTest
    @CsvSource({
            "1, 4, 25.0%",
            "2, 3, 66.7%",
            "0, 4, 0.0%",
            "4, 4, 100.0%",
            "0, 0, 未集計",
            "1000000000, 2000000000, 50.0%"
    })
    void formatsValidCounts(int succeeded, int total, String expected) {
        assertEquals(expected, ImportSuccessRate.format(succeeded, total));
    }

    @ParameterizedTest
    @CsvSource({"-1, 4", "0, -1", "5, 4", "1, 0"})
    void rejectsInvalidCounts(int succeeded, int total) {
        assertThrows(IllegalArgumentException.class,
                () -> ImportSuccessRate.format(succeeded, total));
    }
}
