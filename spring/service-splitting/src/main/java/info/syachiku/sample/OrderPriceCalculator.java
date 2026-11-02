package info.syachiku.sample;
import java.util.List;
import org.springframework.stereotype.Component;
import info.syachiku.sample.OrderData.*;
@Component
public class OrderPriceCalculator {
    public Price calculate(Customer customer, List<Product> products) {
        int subtotal = products.stream().mapToInt(Product::price).sum();
        int discount = customer.premium() ? subtotal / 10 : 0;
        return new Price(subtotal, discount, subtotal - discount);
    }
}
