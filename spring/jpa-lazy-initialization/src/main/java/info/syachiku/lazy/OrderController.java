package info.syachiku.lazy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}/outside")
    public OrderDetails outside(@PathVariable long orderId) {
        PurchaseOrder order = service.load(orderId);
        return OrderDetails.from(order);
    }

    @GetMapping("/{orderId}/inside")
    public OrderDetails inside(@PathVariable long orderId) {
        return service.detailsInsideTransaction(orderId);
    }
}
