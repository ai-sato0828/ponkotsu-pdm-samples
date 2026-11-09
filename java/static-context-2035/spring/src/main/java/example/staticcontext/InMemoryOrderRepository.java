package example.staticcontext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> orders = new LinkedHashMap<>();

    public InMemoryOrderRepository() {
        orders.put(1L, new Order(1L, "PENDING"));
        orders.put(2L, new Order(2L, "PENDING"));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findPendingOrders() {
        return orders.values().stream()
                .filter(order -> order.status().equals("PENDING"))
                .toList();
    }

    @Override
    public void markProcessed(Long id) {
        Order order = findById(id).orElseThrow(OrderNotFoundException::new);
        orders.put(id, new Order(order.id(), "PROCESSED"));
    }
}
