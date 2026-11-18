package info.syachiku.lazy;

import java.util.List;

public record OrderDetails(long orderId, List<Line> lines) {
    public static OrderDetails from(PurchaseOrder order) {
        List<Line> lines = order.getLines().stream()
                .map(line -> new Line(line.getSku(), line.getQuantity()))
                .toList();
        return new OrderDetails(order.getId(), lines);
    }

    public record Line(String sku, int quantity) {
    }
}
