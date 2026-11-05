package info.syachiku.orders;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
class OrderCatalog {
    private static final List<Order> ORDERS = List.of(
            new Order("ORD-101", "OPEN"),
            new Order("ORD-102", "CLOSED"));

    List<Order> find(String status) {
        if (status == null) {
            return ORDERS;
        }
        return ORDERS.stream().filter(order -> order.status().equals(status)).toList();
    }

    record Order(String number, String status) {
    }
}
