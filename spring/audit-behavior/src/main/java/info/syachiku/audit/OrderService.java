package info.syachiku.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository repository;

    OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderStatus shipWithoutTransaction(long orderId) {
        Order order = repository.findById(orderId).orElseThrow();
        order.ship();
        return order.getStatus();
    }

    @Transactional
    public void ship(long orderId) {
        Order order = repository.findById(orderId).orElseThrow();
        order.ship();
    }

    @Transactional
    public void shipAndFailAfterFlush(long orderId) {
        Order order = repository.findById(orderId).orElseThrow();
        order.ship();
        repository.flush();
        throw new IllegalStateException("通知に失敗しました");
    }
}
