package info.syachiku.registration.orders;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderQueryRepository {
    private final JdbcTemplate jdbc;

    public OrderQueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String findStatus(long id) {
        return jdbc.queryForObject("select status from registered_order where id = ?", String.class, id);
    }
}
