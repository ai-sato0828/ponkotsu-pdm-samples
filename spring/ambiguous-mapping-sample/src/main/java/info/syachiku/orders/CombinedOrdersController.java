package info.syachiku.orders;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "orders.mapping", havingValue = "combined", matchIfMissing = true)
class CombinedOrdersController {
    private final OrderCatalog catalog;

    CombinedOrdersController(OrderCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/orders")
    List<OrderCatalog.Order> findOrders(@RequestParam(name = "status", required = false) String status) {
        return catalog.find(status);
    }
}
