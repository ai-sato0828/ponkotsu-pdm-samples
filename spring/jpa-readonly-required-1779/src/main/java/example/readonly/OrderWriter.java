package example.readonly;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
class OrderWriter {
    private final EntityManager entityManager;

    OrderWriter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Observation ship(long id) {
        PurchaseOrder order = entityManager.find(PurchaseOrder.class, id);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        Session session = entityManager.unwrap(Session.class);
        String before = order.status();
        order.ship();
        return new Observation(
                TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                session.getHibernateFlushMode().name(),
                session.isReadOnly(order), entityManager.contains(order), before, order.status());
    }

    record Observation(boolean txReadOnly, String flushMode, boolean entityReadOnly,
                       boolean managed, String before, String inMemory) {}
}
