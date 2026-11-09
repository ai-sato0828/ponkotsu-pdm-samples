package example.staticcontext;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    List<Order> findPendingOrders();
    void markProcessed(Long id);
}
