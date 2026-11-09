package example.staticcontext;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order find(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(OrderNotFoundException::new);
    }

    public void processPendingOrders() {
        for (Order order : orderRepository.findPendingOrders()) {
            orderRepository.markProcessed(order.id());
        }
    }
}
