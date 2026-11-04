package info.syachiku.auditvalidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class OrderController {
    @PostMapping("/orders")
    ResponseEntity<Void> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/no-cascade")
    ResponseEntity<Void> createWithoutCascade(@Valid @RequestBody OrderWithoutCascade request) {
        return ResponseEntity.noContent().build();
    }
}

record OrderWithoutCascade(@NotBlank String customerName,
                           @NotEmpty List<@NotNull OrderItemRequest> items) {
}
