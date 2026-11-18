package info.syachiku.quantity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // BEGIN requests
    public record MinOnlyOrder(@NotNull Long customerId, @Min(1) Integer quantity) {}

    public record RequiredOrder(
            @NotNull Long customerId,
            @NotNull @Min(1) Integer quantity) {}
    // END requests

    // BEGIN endpoints
    @PostMapping("/api/min-only/orders")
    public OrderCommand minOnly(@Valid @RequestBody MinOnlyOrder request) {
        return service.accept(new OrderCommand(request.customerId(), request.quantity()));
    }

    @PostMapping("/api/required/orders")
    public OrderCommand required(@Valid @RequestBody RequiredOrder request) {
        return service.accept(new OrderCommand(request.customerId(), request.quantity()));
    }
    // END endpoints

    public record StrictOrder(
            @NotNull Long customerId,
            @JsonDeserialize(using = JsonIntegerQuantityDeserializer.class)
            @NotNull @Min(1) Integer quantity) {}

    @PostMapping("/api/strict/orders")
    public OrderCommand strict(@Valid @RequestBody StrictOrder request) {
        return service.accept(new OrderCommand(request.customerId(), request.quantity()));
    }
}
