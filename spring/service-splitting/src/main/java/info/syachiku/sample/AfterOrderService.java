package info.syachiku.sample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import info.syachiku.sample.OrderData.*;
@Service
public class AfterOrderService {
    private final OrderPorts ports;
    private final OrderPriceCalculator calculator;
    private final OrderNotifier notifier;
    public AfterOrderService(OrderPorts ports, OrderPriceCalculator calculator, OrderNotifier notifier) {
        this.ports = ports; this.calculator = calculator; this.notifier = notifier;
    }
    @Transactional
    public long placeOrder(Request request) {
        Customer customer = ports.findCustomer(request.customerId());
        var products = ports.findProducts(request.productIds());
        Price price = calculator.calculate(customer, products);
        for (Product product : products) {
            if (product.stock() <= 0) throw new IllegalStateException("out-of-stock:" + product.id());
            ports.decreaseStock(product);
        }
        long orderId = ports.save(customer.id(), price.total());
        notifier.notifyAccepted(customer, orderId);
        ports.writeAudit("ORDER_CREATED", orderId);
        return orderId;
    }
}
