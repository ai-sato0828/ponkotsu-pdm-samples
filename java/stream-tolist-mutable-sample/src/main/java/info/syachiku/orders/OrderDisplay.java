package info.syachiku.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OrderDisplay {
    private OrderDisplay() {
    }

    public static List<DisplayRow> editableRows(List<Order> orders) {
        List<DisplayRow> rows = visibleRows(orders).collect(Collectors.toCollection(ArrayList::new));
        rows.add(totalRow(rows));
        return rows;
    }

    public static List<DisplayRow> completedRows(List<Order> orders) {
        List<DisplayRow> details = visibleRows(orders).toList();
        return Stream.concat(details.stream(), Stream.of(totalRow(details))).toList();
    }

    private static Stream<DisplayRow> visibleRows(List<Order> orders) {
        return orders.stream()
                .filter(Order::visible)
                .map(order -> new DisplayRow(order.number(), order.amountYen()));
    }

    private static DisplayRow totalRow(List<DisplayRow> details) {
        long totalYen = details.stream().mapToLong(DisplayRow::amountYen).sum();
        return new DisplayRow("合計", totalYen);
    }

    public record Order(String number, long amountYen, boolean visible) {
    }

    public record DisplayRow(String label, long amountYen) {
    }
}
