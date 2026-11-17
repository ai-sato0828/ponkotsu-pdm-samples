package info.syachiku.reference;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.resource.jdbc.spi.StatementInspector;

// Measurement-only recorder. Tests and Service calls execute on one thread.
public class SqlCapture implements StatementInspector {
    private static final ThreadLocal<List<String>> SQL = ThreadLocal.withInitial(ArrayList::new);

    public static void begin() {
        SQL.get().clear();
    }

    public static List<String> snapshot() {
        return List.copyOf(SQL.get());
    }

    @Override
    public String inspect(String sql) {
        SQL.get().add(sql);
        return sql;
    }
}
