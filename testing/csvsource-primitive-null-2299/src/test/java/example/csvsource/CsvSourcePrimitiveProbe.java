package example.csvsource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSourcePrimitiveProbe {
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({QuantityCases.PRESENT, QuantityCases.MISSING})
    void quantity(String caseId, int quantity, int expected) {
        System.out.printf("ENTER %s quantity=%s%n", caseId, quantity);
        assertEquals(expected, QuantityRule.normalize(quantity), caseId);
    }
}
