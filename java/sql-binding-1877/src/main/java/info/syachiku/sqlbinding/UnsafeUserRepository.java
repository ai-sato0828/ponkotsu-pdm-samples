package info.syachiku.sqlbinding;

import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

// ローカル比較専用の不良例。実アプリへ採用しないでください。
public class UnsafeUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UnsafeUserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<User> findByName(String name) {
        Objects.requireNonNull(name, "name");
        String sql = "SELECT id, name, email FROM users "
                + "WHERE name = '" + name + "' ORDER BY id";
        return jdbcTemplate.query(sql, UserRepository.USER_MAPPER);
    }
}
