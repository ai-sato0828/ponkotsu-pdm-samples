package example.shipping;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class ShippingQuoteController {
    private final ShippingFee shippingFee;

    ShippingQuoteController(ShippingFee shippingFee) {
        this.shippingFee = shippingFee;
    }

    @GetMapping("/shipping-quote")
    ShippingQuote quote(@RequestParam("subtotal") int subtotal) {
        int fee;
        try {
            fee = shippingFee.calculate(subtotal);
        } catch (IllegalArgumentException invalidSubtotal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidSubtotal.getMessage(), invalidSubtotal);
        }
        return new ShippingQuote(subtotal, fee);
    }

    record ShippingQuote(int subtotal, int shippingFee) {}
}
