public class OrderAlreadyCanceledException extends RuntimeException {
    public OrderAlreadyCanceledException(long orderId) {
        super("Order already canceled. orderId=" + orderId);
    }
}
