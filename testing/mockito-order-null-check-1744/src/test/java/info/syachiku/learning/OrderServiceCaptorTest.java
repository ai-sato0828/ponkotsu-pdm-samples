package info.syachiku.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderServiceCaptorTest {
    @Test
    void capturesCustomerIdAndQuantityPassedToSave() {
        CustomerDirectory customers = mock(CustomerDirectory.class);
        OrderRepository orders = mock(OrderRepository.class);
        OrderService service = new OrderService(customers, orders);
        when(customers.require(10L)).thenReturn(new Customer(10L, true));
        when(orders.save(any(OrderDraft.class))).thenReturn(501L);

        long orderId = service.create(10L, 2);

        ArgumentCaptor<OrderDraft> captor = ArgumentCaptor.forClass(OrderDraft.class);
        verify(orders).save(captor.capture());
        OrderDraft saved = captor.getValue();
        assertEquals(10L, saved.customerId());
        assertEquals(2, saved.quantity());
        assertEquals(501L, orderId);
        System.out.printf("CAPTOR customerId=%d quantity=%d returnedOrderId=%d%n",
                saved.customerId(), saved.quantity(), orderId);
    }
}
