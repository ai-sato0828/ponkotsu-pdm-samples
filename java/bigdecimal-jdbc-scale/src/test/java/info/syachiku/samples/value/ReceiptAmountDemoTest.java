package info.syachiku.samples.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class ReceiptAmountDemoTest {
    private static final BigDecimal INPUT = new BigDecimal("1200.0");

    @Test
    void sqlMatchesTheRowButJdbcReturnsAnAmountWithTwoDecimals() throws SQLException {
        var result = ReceiptAmountDemo.compare(INPUT);

        assertEquals("R001", result.receiptId());
        assertEquals(1, INPUT.scale());
        assertEquals(2, result.storedAmount().scale());
        assertEquals(new BigDecimal("1200.00"), result.storedAmount());
        assertFalse(INPUT.equals(result.storedAmount()));
        assertEquals(0, INPUT.compareTo(result.storedAmount()));
    }

    @Test
    void usingTheAmountsAsTheyAreMissesTheMapEntry() throws SQLException {
        assertNull(ReceiptAmountDemo.compare(INPUT).rawMatch());
    }

    @Test
    void convertingBothKeysToTwoDecimalsFindsTheMapEntry() throws SQLException {
        assertEquals("R001", ReceiptAmountDemo.compare(INPUT).normalizedMatch());
    }

    @Test
    void inputWithAnExtraTrailingZeroStillFitsWithoutRounding() throws SQLException {
        assertEquals("R001", ReceiptAmountDemo.compare(new BigDecimal("1200.000")).normalizedMatch());
    }

    @Test
    void inputThatWouldRequireRoundingIsRejected() {
        assertThrows(ArithmeticException.class, () -> ReceiptAmountDemo.compare(new BigDecimal("1200.001")));
    }
}
