package example.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductCodeTest {
    private final ProductCode first = new ProductCode("A001", 10);
    private final ProductCode second = new ProductCode("A001", 20);

    @Test
    void equalCodesHaveTheSameHashEvenWithDifferentDisplayOrder() {
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashSetKeepsOneEntryForTheSameCode() {
        var set = new HashSet<>(List.of(first, second));
        assertEquals(1, set.size());
    }

    @Test
    void orderedStreamKeepsTheFirstEntryForTheSameCode() {
        List<ProductCode> unique = List.of(first, second).stream().distinct().toList();
        assertEquals(1, unique.size());
        assertSame(first, unique.get(0));
        assertEquals(10, unique.get(0).displayOrder());
    }

    @Test
    void differentCodesStaySeparateEvenWithTheSameDisplayOrder() {
        ProductCode different = new ProductCode("B001", 10);
        List<ProductCode> inputs = List.of(first, different);
        assertNotEquals(first, different);
        assertEquals(2, new HashSet<>(inputs).size());
        assertEquals(2, inputs.stream().distinct().count());
    }
}
