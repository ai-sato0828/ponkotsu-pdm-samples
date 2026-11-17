package info.syachiku.audit;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceIntegrationTest {
    @Autowired
    private OrderRepository repository;
    @Autowired
    private OrderService service;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void prepareWaitingOrder() {
        repository.deleteAll();
        repository.saveAndFlush(new Order(1L, null, BigDecimal.ONE));
    }

    @Test
    void トランザクション外ではJavaの値だけ変わる() {
        assertEquals(OrderStatus.SHIPPED, service.shipWithoutTransaction(1));
        assertEquals(OrderStatus.WAITING, reloadStatus());
    }

    @Test
    void 同じトランザクション内で変更するとコミット後も残る() {
        service.ship(1);
        assertEquals(OrderStatus.SHIPPED, reloadStatus());
    }

    @Test
    void flushでUPDATEした後も例外でロールバックされる() {
        var stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        assertThrows(IllegalStateException.class, () -> service.shipAndFailAfterFlush(1));
        assertEquals(1, stats.getEntityUpdateCount());
        assertEquals(OrderStatus.WAITING, reloadStatus());
    }

    private OrderStatus reloadStatus() {
        return repository.findById(1L).orElseThrow().getStatus();
    }
}
