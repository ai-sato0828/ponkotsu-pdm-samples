package example.deadline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeadlineTest {
    @ParameterizedTest(name = "{0}, {1}: return={2}, missing={3}")
    @CsvSource({
            "2026-11-01, 2026-10-31, false, false",
            "2026-11-01, 2026-11-01, true, true",
            "2026-11-01, 2026-11-07, true, true",
            "2026-11-01, 2026-11-08, true, true",
            "2026-11-01, 2026-11-09, true, false",
            "2026-11-01, 2026-11-14, true, false",
            "2026-11-01, 2026-11-15, true, false",
            "2026-11-01, 2026-11-16, false, false",
            "2026-11-30, 2026-12-07, true, true",
            "2026-11-30, 2026-12-08, true, false"
    })
    void acceptsWithinEachWindow(LocalDate deliveredOn, LocalDate receivedOn,
                                 boolean expectedReturn, boolean expectedMissing) {
        assertEquals(expectedReturn, ReturnDeadline.accepts(deliveredOn, receivedOn), "返品");
        assertEquals(expectedMissing, MissingItemDeadline.accepts(deliveredOn, receivedOn), "商品不足");
    }

    @ParameterizedTest(name = "共通期限{0}日: return={1}, missing={2}")
    @CsvSource({"7, false, false", "14, true, true"})
    void comparesTheFormerSharedRule(long sharedDays,
                                    boolean expectedReturn, boolean expectedMissing) {
        LocalDate deliveredOn = LocalDate.of(2026, 11, 1);
        LocalDate receivedOn = LocalDate.of(2026, 11, 9);
        var oldDesign = new DeadlineDemo.SharedDeadline(sharedDays);
        assertEquals(expectedReturn, oldDesign.acceptsReturn(deliveredOn, receivedOn));
        assertEquals(expectedMissing, oldDesign.acceptsMissingItemReport(deliveredOn, receivedOn));
    }
}
