package example.csvsource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSourceNullableTest {
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({QuantityCases.PRESENT, QuantityCases.MISSING})
    void quantity(String caseId, Integer quantity, int expected) {
        System.out.printf("ENTER %s quantity=%s%n", caseId, quantity);
        assertEquals(expected, QuantityRule.normalize(quantity), caseId);
    }
}
