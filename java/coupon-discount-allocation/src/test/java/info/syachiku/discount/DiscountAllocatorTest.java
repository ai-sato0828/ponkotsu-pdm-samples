package info.syachiku.discount;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountAllocatorTest {
    private final DiscountAllocator allocator = new DiscountAllocator();

    @Test
    void equalLinesKeepCouponTotal() {
        var result = allocator.allocate(List.of(line("A", 100), line("B", 100), line("C", 100)), 100);

        assertEquals(100, result.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(Map.of("A", 34, "B", 33, "C", 33), result);
    }

    @Test
    void sameRemainderDoesNotDependOnInputOrder() {
        OrderLine a = line("A", 100);
        OrderLine b = line("B", 100);
        OrderLine c = line("C", 100);

        assertEquals(allocator.allocate(List.of(a, b, c), 100),
                allocator.allocate(List.of(c, b, a), 100));
    }

    @Test
    void givesOneYenToTheLargestRemainder() {
        var result = allocator.allocate(List.of(line("A", 300), line("B", 200), line("C", 100)), 100);

        assertEquals(Map.of("A", 50, "B", 33, "C", 17), result);
    }

    @Test
    void zeroCouponAssignsZeroToEveryLine() {
        assertEquals(Map.of("A", 0, "B", 0),
                allocator.allocate(List.of(line("A", 30), line("B", 70)), 0));
    }

    @Test
    void zeroPriceLineGetsNoDiscount() {
        var result = allocator.allocate(List.of(line("C", 0), line("B", 100), line("A", 100)), 1);

        assertEquals(0, result.get("C"));
    }

    @Test
    void couponEqualToSubtotalCanCoverEveryLine() {
        assertEquals(Map.of("A", 30, "B", 70),
                allocator.allocate(List.of(line("A", 30), line("B", 70)), 100));
    }

    @Test
    void rejectsCouponAboveSubtotal() {
        assertThrows(IllegalArgumentException.class,
                () -> allocator.allocate(List.of(line("A", 100)), 101));
    }

    @Test
    void rejectsNegativeCoupon() {
        assertThrows(IllegalArgumentException.class,
                () -> allocator.allocate(List.of(line("A", 100)), -1));
    }

    @Test
    void rejectsDuplicateLineId() {
        var lines = List.of(line("A", 40), line("A", 60));

        assertThrows(IllegalArgumentException.class, () -> allocator.allocate(lines, 100));
    }

    @Test
    void rejectsZeroSubtotal() {
        assertThrows(IllegalArgumentException.class,
                () -> allocator.allocate(List.of(line("A", 0)), 0));
    }

    @Test
    void widensSumAndProductBeforeDivision() {
        var lines = List.of(line("A", Integer.MAX_VALUE), line("B", Integer.MAX_VALUE));

        assertEquals(Map.of("A", 1_073_741_824, "B", 1_073_741_823),
                allocator.allocate(lines, Integer.MAX_VALUE));
    }

    @Test
    void rejectsNegativeLineSubtotal() {
        assertThrows(IllegalArgumentException.class, () -> line("A", -1));
    }

    private OrderLine line(String id, int yen) {
        return new OrderLine(id, yen);
    }
}
