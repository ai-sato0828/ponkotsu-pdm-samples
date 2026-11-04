package example.orders;

import example.orders.OrderService.CannotCancelOrderException;
import example.orders.OrderService.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/orders/{id}")
    public OrderResponse detail(@PathVariable("id") long id) {
        return OrderResponse.from(service.detail(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancel(@PathVariable("id") long id) {
        return OrderResponse.from(service.cancel(id));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> orderNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ORDER_NOT_FOUND"));
    }

    @ExceptionHandler(CannotCancelOrderException.class)
    public ResponseEntity<ErrorResponse> orderCannotBeCanceled() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ORDER_CANNOT_BE_CANCELED"));
    }

    public record ErrorResponse(String code) {}
}
