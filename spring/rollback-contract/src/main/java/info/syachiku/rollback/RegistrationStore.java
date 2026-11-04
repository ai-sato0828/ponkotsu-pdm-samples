package info.syachiku.rollback;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class RegistrationStore {
    private final JdbcTemplate jdbc;

    RegistrationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void insertOrder(String requestId) {
        jdbc.update("insert into order_registration (request_id, status) values (?, ?)",
                requestId, "PENDING");
    }

    void updateOrder(String requestId, String status) {
        jdbc.update("update order_registration set status = ? where request_id = ?",
                status, requestId);
    }

    void insertReview(String requestId) {
        jdbc.update("insert into review_record (request_id, result) values (?, ?)",
                requestId, "CHECKED");
    }
}
