public class OrderNotFoundException extends RuntimeException {
    private final long orderId;

    public OrderNotFoundException(long orderId) {
        super("Order not found. orderId=" + orderId);
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }
}
