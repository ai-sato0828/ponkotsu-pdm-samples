package example.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static example.order.OrderStatus.CANCELLED;
import static example.order.OrderStatus.SHIPPED;
import static example.order.OrderStatus.UNSHIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {
    @Test
    void setterLetsShippedOrdersBecomeCancelled() {
        BrokenOrder order = new BrokenOrder();
        order.setStatus(SHIPPED);

        order.setStatus(CANCELLED);

        assertEquals(CANCELLED, order.status());
    }

    @Test
    void startsAsUnshipped() {
        assertEquals(UNSHIPPED, new Order().status());
    }

    @Test
    void canShipAnUnshippedOrder() {
        Order order = new Order();

        order.ship();

        assertEquals(SHIPPED, order.status());
    }

    @Test
    void canCancelAnUnshippedOrder() {
        Order order = new Order();

        order.cancel();

        assertEquals(CANCELLED, order.status());
    }

    @ParameterizedTest(name = "{0}から{1}を拒否して、状態は変えない")
    @CsvSource({
            "SHIPPED, SHIP",
            "SHIPPED, CANCEL",
            "CANCELLED, SHIP",
            "CANCELLED, CANCEL"
    })
    void refusesForbiddenOperationsWithoutChangingStatus(OrderStatus initial, Action action) {
        Order order = new Order();
        if (initial == SHIPPED) {
            order.ship();
        } else {
            order.cancel();
        }

        assertThrows(IllegalStateException.class, () -> action.applyTo(order));
        assertEquals(initial, order.status());
    }

    private enum Action {
        SHIP, CANCEL;

        void applyTo(Order order) {
            switch (this) {
                case SHIP -> order.ship();
                case CANCEL -> order.cancel();
            }
        }
    }
}
