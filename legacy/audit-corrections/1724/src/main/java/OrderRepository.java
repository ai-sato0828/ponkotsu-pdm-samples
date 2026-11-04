import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(long orderId);
}
