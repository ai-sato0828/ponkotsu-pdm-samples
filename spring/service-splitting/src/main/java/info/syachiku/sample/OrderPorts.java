package info.syachiku.sample;
import java.util.List;
import info.syachiku.sample.OrderData.*;
public interface OrderPorts {
    Customer findCustomer(long id);
    List<Product> findProducts(List<Long> ids);
    void decreaseStock(Product product);
    long save(long customerId, int total);
    void sendOrderAccepted(String email, long orderId);
    void writeAudit(String event, long orderId);
}
