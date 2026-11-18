package example.pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.hibernate.resource.jdbc.spi.StatementInspector;

public final class SqlCapture implements StatementInspector {
    private static final ThreadLocal<List<String>> CURRENT_CALL = new ThreadLocal<>();

    @Override
    public String inspect(String sql) {
        List<String> statements = CURRENT_CALL.get();
        if (statements != null) {
            statements.add(sql);
        }
        return sql;
    }

    static <T> Execution<T> capture(Supplier<T> query) {
        List<String> statements = new ArrayList<>();
        CURRENT_CALL.set(statements);
        try {
            return new Execution<>(query.get(), List.copyOf(statements));
        } finally {
            CURRENT_CALL.remove();
        }
    }

    record Execution<T>(T result, List<String> statements) {
    }
}
