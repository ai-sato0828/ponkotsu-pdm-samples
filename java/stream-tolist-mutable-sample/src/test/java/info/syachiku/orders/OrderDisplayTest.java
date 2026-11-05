package info.syachiku.orders;

import java.util.ArrayList;
import java.util.List;

import info.syachiku.orders.OrderDisplay.DisplayRow;
import info.syachiku.orders.OrderDisplay.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderDisplayTest {
    private static final Order FIRST = new Order("A-101", 1_200, true);
    private static final Order HIDDEN = new Order("A-102", 5_000, false);
    private static final Order LAST = new Order("A-103", 800, true);
    private static final List<Order> ORDERS = List.of(FIRST, HIDDEN, LAST);
    private static final DisplayRow TOTAL = new DisplayRow("合計", 2_000);
    private static final DisplayRow NOTE = new DisplayRow("備考", 0);
    private static final List<DisplayRow> EXPECTED = List.of(
            new DisplayRow("A-101", 1_200),
            new DisplayRow("A-103", 800),
            TOTAL);

    @Test
    void addingToStreamToListThrows() {
        List<DisplayRow> rows = ORDERS.stream()
                .filter(Order::visible)
                .map(order -> new DisplayRow(order.number(), order.amountYen()))
                .toList();

        assertThrows(UnsupportedOperationException.class, () -> rows.add(TOTAL));
    }

    @Test
    void editableListContainsSummary() {
        assertEquals(EXPECTED, OrderDisplay.editableRows(ORDERS));
    }

    @Test
    void editableListCanBeEditedAfterBuilding() {
        var rows = OrderDisplay.editableRows(ORDERS);
        rows.add(NOTE);

        assertEquals(4, rows.size());
        assertEquals(NOTE, rows.get(3));
    }

    @Test
    void completedListContainsSummaryAndRejectsAppend() {
        var rows = OrderDisplay.completedRows(ORDERS);

        assertEquals(EXPECTED, rows);
        assertThrows(UnsupportedOperationException.class, () -> rows.add(NOTE));
    }

    @Test
    void bothChoicesLeaveTheSourceListUntouched() {
        var original = new ArrayList<>(ORDERS);
        var edited = OrderDisplay.editableRows(original);
        var completed = OrderDisplay.completedRows(original);
        edited.add(NOTE);

        assertEquals(ORDERS, original);
        assertEquals(EXPECTED, completed);
    }

    @Test
    void zeroVisibleOrdersProducesZeroTotal() {
        var zero = List.of(new DisplayRow("合計", 0));

        assertEquals(zero, OrderDisplay.editableRows(List.of(HIDDEN)));
        assertEquals(zero, OrderDisplay.completedRows(List.of(HIDDEN)));
    }
}
