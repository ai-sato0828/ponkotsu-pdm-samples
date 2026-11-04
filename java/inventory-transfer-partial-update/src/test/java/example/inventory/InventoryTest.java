package example.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryTest {
    @Test
    void observesThatTheBrokenVersionLosesStockOnFailure() {
        BrokenInventory inventory = new BrokenInventory(8, 9, 10);

        assertThrows(IllegalStateException.class, () -> inventory.move(2));

        assertEquals(new StockLevels(6, 9), inventory.snapshot());
    }

    @Test
    void preservesBothCountsWhenTheDestinationHasNoRoom() {
        Inventory inventory = new Inventory(8, 9, 10);
        StockLevels before = inventory.snapshot();

        assertThrows(IllegalStateException.class, () -> inventory.move(2));

        assertEquals(before, inventory.snapshot());
    }

    @Test
    void movesExactlyTheRemainingCapacityAndKeepsTheTotal() {
        Inventory inventory = new Inventory(8, 9, 10);
        StockLevels before = inventory.snapshot();

        inventory.move(1);

        StockLevels after = inventory.snapshot();
        assertEquals(new StockLevels(7, 10), after);
        assertEquals(before.source() + before.destination(), after.source() + after.destination());
    }

    @Test
    void preservesBothCountsWhenTheSourceIsShort() {
        Inventory inventory = new Inventory(1, 0, 10);

        assertThrows(IllegalStateException.class, () -> inventory.move(2));

        assertEquals(new StockLevels(1, 0), inventory.snapshot());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveMovesWithoutChangingCounts(int units) {
        Inventory inventory = new Inventory(8, 9, 10);

        assertThrows(IllegalArgumentException.class, () -> inventory.move(units));

        assertEquals(new StockLevels(8, 9), inventory.snapshot());
    }

    @Test
    void canMoveOneUnitAfterRejectingTwo() {
        Inventory inventory = new Inventory(8, 9, 10);
        assertThrows(IllegalStateException.class, () -> inventory.move(2));

        inventory.move(1);

        assertEquals(new StockLevels(7, 10), inventory.snapshot());
    }
}
