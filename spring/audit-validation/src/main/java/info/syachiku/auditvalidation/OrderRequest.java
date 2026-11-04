package info.syachiku.auditvalidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

record OrderRequest(
        @NotBlank String customerName,
        @NotEmpty List<@NotNull @Valid OrderItemRequest> items) {
}

record OrderItemRequest(@NotBlank String sku, @Min(1) int quantity) {
}
