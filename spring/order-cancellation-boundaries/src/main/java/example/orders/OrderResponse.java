package example.orders;

import example.orders.OrderService.OrderResult;

public record OrderResponse(long id, String status, boolean cancelable) {
    public static OrderResponse from(OrderResult result) {
        String status = switch (result.status()) {
            case WAITING -> "WAITING";
            case SHIPPED -> "SHIPPED";
            case CANCELED -> "CANCELED";
        };
        return new OrderResponse(result.id(), status, result.cancelable());
    }
}
