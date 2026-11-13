package info.syachiku.order500;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public OrderView save(String item, String requestId) {
        String orderId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO orders(order_id, item, request_id) VALUES (?, ?, ?)",
                orderId, item, requestId);
        return new OrderView(orderId, item, requestId);
    }

    @Transactional(readOnly = true)
    public List<OrderView> findByRequestId(String requestId) {
        return jdbc.query("SELECT order_id, item, request_id FROM orders WHERE request_id = ? ORDER BY order_id",
                (row, number) -> new OrderView(row.getString("order_id"),
                        row.getString("item"), row.getString("request_id")), requestId);
    }
}
