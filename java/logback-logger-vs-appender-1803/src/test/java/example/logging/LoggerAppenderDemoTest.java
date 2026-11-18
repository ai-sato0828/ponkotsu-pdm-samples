package example.logging;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggerAppenderDemoTest {
    private static final String DEBUG = "DEBUG:lookup started sku=SKU-001";
    private static final String INFO = "INFO:lookup complete sku=SKU-001";

    @Test
    void inheritedInfoDisablesDebugBeforeTheRootAppender() {
        var result = LoggerAppenderDemo.inheritedInfo();
        assertEquals("INFO", result.rootLevel());
        assertEquals("INHERITED", result.childLevel());
        assertEquals("INFO", result.effectiveLevel());
        assertFalse(result.debugEnabled());
        assertEquals(List.of(INFO), result.events());
    }

    @Test
    void explicitChildDebugReachesTheRootAppenderDespiteRootInfo() {
        var result = LoggerAppenderDemo.explicitDebug();
        assertEquals("INFO", result.rootLevel());
        assertEquals("DEBUG", result.childLevel());
        assertEquals("DEBUG", result.effectiveLevel());
        assertTrue(result.debugEnabled());
        assertEquals(List.of(DEBUG, INFO), result.events());
    }

    @Test
    void infoThresholdRemovesDebugAfterTheLoggerEnabledIt() {
        var result = LoggerAppenderDemo.debugWithInfoThreshold();
        assertEquals("INFO", result.rootLevel());
        assertEquals("DEBUG", result.childLevel());
        assertEquals("DEBUG", result.effectiveLevel());
        assertTrue(result.debugEnabled());
        assertEquals(List.of(INFO), result.events());
    }
}
