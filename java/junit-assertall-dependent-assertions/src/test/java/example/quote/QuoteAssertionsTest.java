package example.quote;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

import java.util.List;

import static example.quote.Quote.Method.EXPRESS;
import static example.quote.Quote.Method.STANDARD;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteAssertionsTest {
    private final QuoteCalculator calculator = new QuoteCalculator();

    @Test
    void separateLambdasAlsoDereferenceMissingDelivery() {
        Quote quote = calculator.calculate(4_200, null);

        var failures = reportedFailures(() -> checkSeparately(quote));
        showFailures("separateMissing", failures);
        assertEquals(3, failures.size());
        assertFailureLabel(failures.get(0), "見積合計");
        assertFailureLabel(failures.get(1), "配送情報");
        assertInstanceOf(NullPointerException.class, failures.get(2));
    }

    @Test
    void groupedDeliveryKeepsTheIndependentTotalCheck() {
        Quote quote = calculator.calculate(4_200, null);

        var failures = reportedFailures(() -> checkGrouped(quote));
        showFailures("groupedMissing", failures);
        assertEquals(2, failures.size());
        assertFailureLabel(failures.get(0), "配送情報");
        assertFailureLabel(failures.get(1), "見積合計");
    }

    @Test
    void groupedDeliveryStillChecksTheMethodWhenDeliveryExists() {
        Quote quote = calculator.calculate(4_000, new Quote.Delivery(EXPRESS, 700));

        var failures = reportedFailures(() -> checkGrouped(quote));
        showFailures("groupedWrongMethod", failures);
        assertEquals(1, failures.size());
        assertFailureLabel(failures.get(0), "配送方法");
    }

    @Test
    void completeStandardQuotePasses() {
        Quote quote = calculator.calculate(4_200, new Quote.Delivery(STANDARD, 500));

        assertDoesNotThrow(() -> checkGrouped(quote));
    }

    private static void checkSeparately(Quote quote) {
        assertAll("確定見積",
                () -> assertEquals(4_700, quote.totalYen(), "見積合計"),
                () -> assertNotNull(quote.delivery(), "配送情報"),
                () -> assertEquals(STANDARD, quote.delivery().method(), "配送方法"));
    }

    private static void checkGrouped(Quote quote) {
        assertAll("確定見積",
                () -> {
                    Quote.Delivery delivery = quote.delivery();
                    assertNotNull(delivery, "配送情報");
                    assertEquals(STANDARD, delivery.method(), "配送方法");
                },
                () -> assertEquals(4_700, quote.totalYen(), "見積合計"));
    }

    private static List<Throwable> reportedFailures(org.junit.jupiter.api.function.Executable check) {
        return assertThrows(MultipleFailuresError.class, check).getFailures();
    }

    private static void assertFailureLabel(Throwable failure, String label) {
        var assertion = assertInstanceOf(AssertionFailedError.class, failure);
        assertTrue(assertion.getMessage().startsWith(label + " ==>"), assertion::getMessage);
    }

    private static void showFailures(String label, List<Throwable> failures) {
        var names = failures.stream()
                .map(failure -> failure instanceof AssertionFailedError
                        ? failure.getMessage().split(" ==> ", 2)[0]
                        : failure.getClass().getSimpleName())
                .toList();
        System.out.println(label + " = " + String.join(" / ", names));
    }
}
