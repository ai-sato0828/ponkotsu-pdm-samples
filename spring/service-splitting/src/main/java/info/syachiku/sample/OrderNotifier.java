package info.syachiku.sample;
import org.springframework.stereotype.Component;
import info.syachiku.sample.OrderData.Customer;
@Component
public class OrderNotifier {
    private final OrderPorts ports;
    public OrderNotifier(OrderPorts ports) { this.ports = ports; }
    public void notifyAccepted(Customer customer, long orderId) {
        ports.sendOrderAccepted(customer.email(), orderId);
    }
}
