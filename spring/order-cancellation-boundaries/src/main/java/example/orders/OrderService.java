package example.orders;

import org.springframework.stereotype.Service;

@Service
public final class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderResult detail(long id) {
        return OrderResult.from(requireOrder(id));
    }

    public OrderResult cancel(long id) {
        Order order = requireOrder(id);
        if (!order.canCancel()) {
            throw new CannotCancelOrderException();
        }
        Order canceled = order.canceled();
        repository.save(canceled);
        return OrderResult.from(canceled);
    }

    private Order requireOrder(long id) {
        return repository.findById(id).orElseThrow(OrderNotFoundException::new);
    }

    public record OrderResult(long id, Order.Status status, boolean cancelable) {
        private static OrderResult from(Order order) {
            return new OrderResult(order.id(), order.status(), order.canCancel());
        }
    }

    public static final class OrderNotFoundException extends RuntimeException {}
    public static final class CannotCancelOrderException extends RuntimeException {}
}
