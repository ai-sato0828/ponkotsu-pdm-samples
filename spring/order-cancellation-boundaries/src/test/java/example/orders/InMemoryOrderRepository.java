package example.orders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> orders = new HashMap<>();
    int reads;
    int saves;

    void reset(Order... initialOrders) {
        orders.clear();
        for (Order order : initialOrders) {
            orders.put(order.id(), order);
        }
        reads = 0;
        saves = 0;
    }

    Order stored(long id) {
        return orders.get(id);
    }

    @Override
    public Optional<Order> findById(long id) {
        reads++;
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public void save(Order order) {
        saves++;
        orders.put(order.id(), order);
    }
}
