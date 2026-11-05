package info.syachiku.orders;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "orders.mapping", havingValue = "params")
class ParameterOrdersController {
    private final OrderCatalog catalog;

    ParameterOrdersController(OrderCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping(path = "/orders", params = "!status")
    List<OrderCatalog.Order> list() {
        return catalog.find(null);
    }

    @GetMapping(path = "/orders", params = "status")
    List<OrderCatalog.Order> search(@RequestParam("status") String status) {
        return catalog.find(status);
    }
}
