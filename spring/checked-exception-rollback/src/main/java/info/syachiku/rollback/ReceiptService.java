package info.syachiku.rollback;

import java.io.IOException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceiptService {
    private final JdbcTemplate jdbc;

    public ReceiptService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void register(String orderId, ReceiptOutput output) throws IOException {
        insertThenWrite(orderId, output);
    }

    @Transactional(rollbackFor = IOException.class)
    public void registerRollbackOnIo(String orderId, ReceiptOutput output) throws IOException {
        insertThenWrite(orderId, output);
    }

    private void insertThenWrite(String orderId, ReceiptOutput output) throws IOException {
        jdbc.update("INSERT INTO receipt_registrations (order_id) VALUES (?)", orderId);
        output.write();
    }

    @FunctionalInterface
    public interface ReceiptOutput {
        void write() throws IOException;
    }
}
