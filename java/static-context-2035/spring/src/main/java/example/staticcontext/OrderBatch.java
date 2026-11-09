package example.staticcontext;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OrderBatch implements CommandLineRunner {

    private final OrderService orderService;

    public OrderBatch(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        orderService.processPendingOrders();
    }
}
