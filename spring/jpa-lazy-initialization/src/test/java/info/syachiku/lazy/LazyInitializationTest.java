package info.syachiku.lazy;

import jakarta.servlet.ServletException;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LazyInitializationTest {
    @Autowired
    private OrderService service;

    @Autowired
    private MockMvc mvc;

    @Test
    @Transactional
    void testTransactionKeepsTheLazyLinesReadable() {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        PurchaseOrder order = service.load(42);
        assertFalse(Hibernate.isInitialized(order.getLines()));

        assertEquals(new OrderDetails(42, List.of(
                new OrderDetails.Line("BOOK", 2), new OrderDetails.Line("PEN", 1))), OrderDetails.from(order));
        assertTrue(Hibernate.isInitialized(order.getLines()));
    }

    @Test
    void repositoryReturnDoesNotInitializeTheLinesWithoutAnOuterTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        PurchaseOrder order = service.load(42);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertFalse(Hibernate.isInitialized(order.getLines()));

        assertThrows(LazyInitializationException.class, () -> OrderDetails.from(order));
    }

    @Test
    void outsideEndpointExposesTheSameFailureWithoutATestTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        ServletException failure = assertThrows(ServletException.class,
                () -> mvc.perform(get("/orders/42/outside")));
        assertInstanceOf(LazyInitializationException.class, failure.getCause());
    }

    @Test
    void insideEndpointReturnsACompleteDtoWithoutATestTransaction() throws Exception {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        mvc.perform(get("/orders/42/inside"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"orderId":42,"lines":[{"sku":"BOOK","quantity":2},{"sku":"PEN","quantity":1}]}
                        """, JsonCompareMode.STRICT));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
