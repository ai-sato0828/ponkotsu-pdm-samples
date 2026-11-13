package info.syachiku.order500;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    private final IndividualCommitOrderService individual;
    private final TransactionalOrderService transactional;
    private final OrderRepository repository;

    public OrderController(IndividualCommitOrderService individual,
            TransactionalOrderService transactional, OrderRepository repository) {
        this.individual = individual;
        this.transactional = transactional;
        this.repository = repository;
    }

    @PostMapping("/api/orders/individual")
    public ResponseEntity<OrderView> createIndividual(@Valid @RequestBody CreateOrderRequest body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(individual.create(body, RequestIdFilter.current(request)));
    }

    @PostMapping("/api/orders/transactional")
    public ResponseEntity<OrderView> createTransactional(@Valid @RequestBody CreateOrderRequest body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactional.create(body, RequestIdFilter.current(request)));
    }

    @GetMapping("/api/orders/by-request/{requestId}")
    public OrderReadback readByRequest(@PathVariable String requestId) {
        List<OrderView> orders = repository.findByRequestId(requestId);
        return new OrderReadback(requestId, orders.size(), orders);
    }
}
