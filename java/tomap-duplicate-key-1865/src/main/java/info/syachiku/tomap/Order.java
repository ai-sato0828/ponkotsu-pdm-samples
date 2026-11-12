package info.syachiku.tomap;

import java.util.Objects;

public record Order(String orderId, String customerId, String status) {
    public Order {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(status, "status");
    }
}
