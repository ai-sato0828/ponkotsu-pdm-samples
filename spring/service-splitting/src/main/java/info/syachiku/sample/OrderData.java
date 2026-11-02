package info.syachiku.sample;
import java.util.List;
public final class OrderData {
    private OrderData() {}
    public record Customer(long id, boolean premium, String email) {}
    public record Product(long id, int price, int stock) {}
    public record Request(long customerId, List<Long> productIds) {}
    public record Price(int subtotal, int discount, int total) {}
}
