package info.syachiku.orders;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

public final class OrderPreviewControllers {

    private OrderPreviewControllers() {
    }

    @RestController
    public static class JsonOnlyController {
        private final OrderPreviewService service;

        public JsonOnlyController(OrderPreviewService service) {
            this.service = service;
        }

        @PostMapping(path = "/orders/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
        public OrderPreview preview(@RequestBody OrderRequest request) {
            return service.preview(request);
        }
    }

    @RestController
    public static class OrderVendorController {
        private final OrderPreviewService service;

        public OrderVendorController(OrderPreviewService service) {
            this.service = service;
        }

        @PostMapping(path = "/orders/preview", consumes = {
                MediaType.APPLICATION_JSON_VALUE, "application/vnd.example.order+json"
        })
        public OrderPreview preview(@RequestBody OrderRequest request) {
            return service.preview(request);
        }
    }

    @FunctionalInterface
    public interface OrderPreviewService {
        OrderPreview preview(OrderRequest request);
    }

    public record OrderRequest(String orderId, int quantity) {
    }

    public record OrderPreview(String orderId, int quantity) {
    }
}
