package example.orders;

import example.orders.OrderService.CannotCancelOrderException;
import example.orders.OrderService.OrderNotFoundException;
import org.junit.jupiter.api.Test;

import static example.orders.Order.Status.CANCELED;
import static example.orders.Order.Status.SHIPPED;
import static example.orders.Order.Status.WAITING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final OrderService service = new OrderService(repository);

    @Test
    void waitingOrderIsCanceledAndSavedOnce() {
        repository.reset(new Order(100, WAITING, "REF-100"));

        var result = service.cancel(100);

        assertEquals(CANCELED, result.status());
        assertFalse(result.cancelable());
        assertEquals(new Order(100, CANCELED, "REF-100"), repository.stored(100));
        assertEquals(1, repository.saves);
    }

    @Test
    void shippedOrderIsRejectedWithoutSaving() {
        repository.reset(new Order(101, SHIPPED, "REF-101"));

        assertThrows(CannotCancelOrderException.class, () -> service.cancel(101));

        assertEquals(SHIPPED, repository.stored(101).status());
        assertEquals(0, repository.saves);
    }

    @Test
    void canceledOrderIsRejectedWithoutSaving() {
        repository.reset(new Order(102, CANCELED, "REF-102"));

        assertThrows(CannotCancelOrderException.class, () -> service.cancel(102));

        assertEquals(CANCELED, repository.stored(102).status());
        assertEquals(0, repository.saves);
    }

    @Test
    void missingOrderIsRejectedWithoutSaving() {
        repository.reset();

        assertThrows(OrderNotFoundException.class, () -> service.cancel(999));

        assertEquals(0, repository.saves);
    }
}
