package example.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryTotalTest {
    @Test
    void assigningToLongAfterMultiplicationDoesNotRecoverLostBits() {
        assertEquals(-1_794_967_296L, BrokenInventoryTotal.total(50_000, 50_000));
        assertEquals(2_500_000_000L, InventoryTotal.total(50_000, 50_000));
    }

    @Test
    void valuesAroundTheIntBoundaryKeepTheirLongProduct() {
        assertEquals(2_147_395_600L, InventoryTotal.total(46_340, 46_340));
        assertEquals(2_147_488_281L, InventoryTotal.total(46_341, 46_341));
    }

    @Test
    void largestPositiveIntInputsStillFitInLong() {
        assertEquals(4_611_686_014_132_420_609L,
                InventoryTotal.total(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void intColumnAcceptsTheMaximumAndRejectsOverflow() {
        assertEquals(Integer.MAX_VALUE, InventoryTotal.totalForIntColumn(Integer.MAX_VALUE, 1));
        assertEquals(2_147_395_600, InventoryTotal.totalForIntColumn(46_340, 46_340));
        assertThrows(ArithmeticException.class, () -> InventoryTotal.totalForIntColumn(46_341, 46_341));
    }

    @Test
    void longInputsAlsoHaveALimit() {
        assertEquals(Long.MAX_VALUE, InventoryTotal.totalFromLongCounts(Long.MAX_VALUE, 1));
        assertThrows(ArithmeticException.class, () -> InventoryTotal.totalFromLongCounts(Long.MAX_VALUE, 2));
    }

    @ParameterizedTest
    @CsvSource({"0, 50", "50, 0"})
    void acceptsZero(int boxes, int itemsPerBox) {
        assertEquals(0L, InventoryTotal.total(boxes, itemsPerBox));
        assertEquals(0, InventoryTotal.totalForIntColumn(boxes, itemsPerBox));
        assertEquals(0L, InventoryTotal.totalFromLongCounts(boxes, itemsPerBox));
    }

    @ParameterizedTest
    @CsvSource({"-1, 50", "50, -1"})
    void rejectsNegativeInputEvenWhenTheProductFits(int boxes, int itemsPerBox) {
        assertThrows(IllegalArgumentException.class, () -> InventoryTotal.total(boxes, itemsPerBox));
        assertThrows(IllegalArgumentException.class, () -> InventoryTotal.totalForIntColumn(boxes, itemsPerBox));
        assertThrows(IllegalArgumentException.class, () -> InventoryTotal.totalFromLongCounts(boxes, itemsPerBox));
    }
}
