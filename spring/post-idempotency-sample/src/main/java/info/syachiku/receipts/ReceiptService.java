package info.syachiku.receipts;

import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ReceiptService {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public ReceiptService(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public Registration register(UUID requestId, String title) {
        var incoming = new Receipt(requestId, title);
        try {
            transactions.executeWithoutResult(status ->
                    jdbc.update("INSERT INTO application_receipt (request_id, title) VALUES (?, ?)",
                            requestId, title));
            return new Registration(incoming, true);
        } catch (DuplicateKeyException ex) {
            var existing = Objects.requireNonNull(transactions.execute(status ->
                    jdbc.queryForObject("SELECT request_id, title FROM application_receipt WHERE request_id = ?",
                            (row, index) -> new Receipt(row.getObject("request_id", UUID.class),
                                    row.getString("title")), requestId)));

            if (!existing.title().equals(title)) {
                throw new RequestIdConflict();
            }
            return new Registration(existing, false);
        }
    }

    public record Receipt(UUID requestId, String title) {
    }

    public record Registration(Receipt receipt, boolean created) {
    }

    public static final class RequestIdConflict extends RuntimeException {
    }
}
