package info.syachiku.sample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import info.syachiku.sample.OrderData.*;
@Service
public class BeforeOrderService {
    private final OrderPorts ports;
    public BeforeOrderService(OrderPorts ports) { this.ports = ports; }
    @Transactional
    public long placeOrder(Request request) {
        Customer customer = ports.findCustomer(request.customerId());
        var products = ports.findProducts(request.productIds());
        int subtotal = products.stream().mapToInt(Product::price).sum();
        int discount = customer.premium() ? subtotal / 10 : 0;
        int total = subtotal - discount;
        for (Product product : products) {
            if (product.stock() <= 0) throw new IllegalStateException("out-of-stock:" + product.id());
            ports.decreaseStock(product);
        }
        long orderId = ports.save(customer.id(), total);
        ports.sendOrderAccepted(customer.email(), orderId);
        ports.writeAudit("ORDER_CREATED", orderId);
        return orderId;
    }
}
