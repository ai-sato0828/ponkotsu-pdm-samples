package info.syachiku.registration.payment.web;

import info.syachiku.registration.payment.sales.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/orders/{id}/authorization")
    String authorize(@PathVariable("id") String id) {
        return service.authorize(id);
    }
}
