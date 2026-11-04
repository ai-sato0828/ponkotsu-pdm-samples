import java.util.Optional;

public class OrderServiceCheck {
    public static void main(String[] args) {
        Order order = new Order();
        var service = new OrderService(id -> id == 7
                ? Optional.of(order)
                : Optional.empty());

        try {
            service.cancel(8);
            throw new AssertionError("missing order accepted");
        } catch (OrderNotFoundException expected) {
            if (expected.getOrderId() != 8) {
                throw new AssertionError("missing order ID lost");
            }
        }

        service.cancel(7);
        if (!order.isCanceled()) {
            throw new AssertionError("cancellation was not applied");
        }

        try {
            service.cancel(7);
            throw new AssertionError("duplicate cancellation accepted");
        } catch (OrderAlreadyCanceledException expected) {
            if (!expected.getMessage().contains("orderId=7")) {
                throw new AssertionError("canceled order ID lost");
            }
        }

        System.out.println("3 checks passed");
    }
}
