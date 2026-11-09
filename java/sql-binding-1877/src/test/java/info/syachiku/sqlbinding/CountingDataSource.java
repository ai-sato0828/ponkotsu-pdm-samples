package info.syachiku.sqlbinding;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

// テスト補助。接続取得0なら、このDataSource経由のSQL実行も0です。
final class CountingDataSource extends AbstractDataSource {
    private final DataSource delegate;
    private int connectionRequests;

    CountingDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    int connectionRequests() {
        return connectionRequests;
    }

    @Override
    public Connection getConnection() throws SQLException {
        connectionRequests++;
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        connectionRequests++;
        return delegate.getConnection(username, password);
    }
}
