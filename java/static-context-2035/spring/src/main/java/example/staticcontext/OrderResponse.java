package example.staticcontext;

public record OrderResponse(Long id, String status) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.id(), order.status());
    }
}
