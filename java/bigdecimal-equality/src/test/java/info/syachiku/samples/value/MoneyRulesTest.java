package info.syachiku.samples.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class MoneyRulesTest {
    @ParameterizedTest(name = "{0}: sameAmount({1},{2})={3}")
    @CsvSource({"B01,0.0,0.00,true", "B02,1200.0,1200.00,true",
            "B03,1200,1200.01,false", "B04,-1.0,-1.00,true"})
    void numericEquality(String id, String left, String right, boolean expected) {
        assertEquals(expected, MoneyRules.sameAmount(new BigDecimal(left), new BigDecimal(right)), id);
    }
    @Test void B05_nullLeftIsNotZero() {
        assertThrows(NullPointerException.class, () -> MoneyRules.sameAmount(null, BigDecimal.ZERO));
    }
    @Test void B06_nullRightIsNotZero() {
        assertThrows(NullPointerException.class, () -> MoneyRules.sameAmount(BigDecimal.ZERO, null));
    }
    @ParameterizedTest(name = "normalize {0} -> {1}")
    @CsvSource({"0,0.00", "1200,1200.00", "1200.0,1200.00",
            "1200.00,1200.00", "1200.000,1200.00"})
    void N01_N05_normalizeWithoutRounding(String input, String expected) {
        assertEquals(new BigDecimal(expected), MoneyRules.fixedTwoDecimals(new BigDecimal(input)));
    }
    @ParameterizedTest
    @ValueSource(strings = {"1200.001", "-1.234"})
    void N06_N07_nonZeroExtraDigitsAreRejected(String input) {
        assertThrows(ArithmeticException.class, () -> MoneyRules.fixedTwoDecimals(new BigDecimal(input)));
    }
    @Test void N08_missingAmountIsRejected() {
        assertThrows(NullPointerException.class, () -> MoneyRules.fixedTwoDecimals(null));
    }
    @Test void C01_hashSetDoesNotUseNumericComparison() {
        Set<BigDecimal> values = new HashSet<>();
        values.add(new BigDecimal("1200.0"));
        values.add(new BigDecimal("1200.00"));
        assertEquals(2, values.size());
        assertFalse(values.contains(new BigDecimal("1200")));
    }
    @Test void C02_hashMapLookupUsesEquals() {
        Map<BigDecimal, String> values = new HashMap<>();
        values.put(new BigDecimal("1200.0"), "paid");
        assertNull(values.get(new BigDecimal("1200.00")));
    }
    @Test void C03_naturalTreeSetUsesOrdering() {
        Set<BigDecimal> values = new TreeSet<>();
        values.add(new BigDecimal("1200.0"));
        values.add(new BigDecimal("1200.00"));
        assertEquals(1, values.size());
    }
    @Test void C04_normalizeAtInsertionAndLookup() {
        Set<BigDecimal> values = new HashSet<>();
        values.add(MoneyRules.fixedTwoDecimals(new BigDecimal("1200.0")));
        values.add(MoneyRules.fixedTwoDecimals(new BigDecimal("1200.00")));
        assertEquals(1, values.size());
        assertTrue(values.contains(MoneyRules.fixedTwoDecimals(new BigDecimal("1200"))));
        Map<BigDecimal, String> map = new HashMap<>();
        map.put(MoneyRules.fixedTwoDecimals(new BigDecimal("1200.0")), "paid");
        assertEquals("paid", map.get(MoneyRules.fixedTwoDecimals(new BigDecimal("1200.00"))));
    }
    @Test void R01_roundingBeforeComparisonCanHideDifference() {
        BigDecimal left = new BigDecimal("0.004");
        BigDecimal right = new BigDecimal("0.003");
        assertFalse(MoneyRules.sameAmount(left, right));
        assertEquals(left.setScale(2, RoundingMode.HALF_UP), right.setScale(2, RoundingMode.HALF_UP));
    }
    @Test void D01_doubleConstructorCarriesBinaryApproximation() {
        assertNotEquals(new BigDecimal("0.1"), new BigDecimal(0.1));
        assertEquals(new BigDecimal("0.1"), BigDecimal.valueOf(0.1));
    }
    @ParameterizedTest(name = "{0}: amount={1}, withinLimit={2}")
    @CsvSource({"L01,1199.99,true", "L02,1200.00,true", "L03,1200.01,false"})
    void upperLimitIncludesEquality(String id, String input, boolean expected) {
        assertEquals(expected, MoneyRules.withinLimit(new BigDecimal(input), new BigDecimal("1200")), id);
    }
    @Test void L04_missingLimitIsRejected() {
        assertThrows(NullPointerException.class,
                () -> MoneyRules.withinLimit(BigDecimal.ZERO, null));
    }
}
