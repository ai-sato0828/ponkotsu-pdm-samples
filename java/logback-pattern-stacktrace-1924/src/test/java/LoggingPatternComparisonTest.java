import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingPatternComparisonTest {
    private static final String MESSAGE = "在庫の保存に失敗 id=I-17";
    private static final String EXCEPTION = "java.lang.IllegalStateException: stock write failed";

    @Test
    void eventRetainsThrowableBeforeFormatting() {
        assertEquals("java.lang.IllegalStateException",
                LoggingPatternComparison.compare().throwableType());
    }

    @Test
    void nopexOutputsOnlyTheMessage() {
        assertEquals(MESSAGE + System.lineSeparator(),
                LoggingPatternComparison.compare().hidden());
    }

    @Test
    void exOutputsTheMessageExceptionTypeAndStackFrame() {
        assertIncludesStackTrace(LoggingPatternComparison.compare().explicit());
    }

    @Test
    void omittingThrowableConversionKeepsTheDefaultTrace() {
        assertIncludesStackTrace(LoggingPatternComparison.compare().automatic());
    }

    private static void assertIncludesStackTrace(String output) {
        var lines = output.lines().toList();
        assertEquals(MESSAGE, lines.get(0));
        assertEquals(EXCEPTION, lines.get(1));
        assertTrue(lines.get(2).stripLeading().startsWith("at LoggingPatternComparison.saveInventory("));
    }
}
