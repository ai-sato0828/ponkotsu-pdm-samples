package info.syachiku.sqlbinding;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class UserRepository {
    static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> new User(
            rs.getString("id"), rs.getString("name"), rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<User> findByName(String name) {
        Objects.requireNonNull(name, "name");
        String sql = """
                SELECT id, name, email FROM users
                WHERE name = ? ORDER BY id
                """;
        return jdbcTemplate.query(sql, USER_MAPPER, name);
    }

    public List<User> findByStatusFrom(String status, LocalDate from) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(from, "from");
        String sql = """
                SELECT id, name, email FROM users
                WHERE status = ? AND created_at >= ? ORDER BY id
                """;
        return jdbcTemplate.query(sql, USER_MAPPER, status, Date.valueOf(from));
    }

    public List<User> findByStatusFromNamed(String status, LocalDate from) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(from, "from");
        String sql = """
                SELECT id, name, email FROM users
                WHERE status = :status AND created_at >= :from ORDER BY id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("from", Date.valueOf(from));
        return namedParameterJdbcTemplate.query(sql, params, USER_MAPPER);
    }

    public List<User> findByNameLike(String keyword) {
        Objects.requireNonNull(keyword, "keyword");
        String sql = """
                SELECT id, name, email FROM users
                WHERE name LIKE ? ORDER BY id
                """;
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, USER_MAPPER, pattern);
    }

    public List<User> findAllSorted(String sortKey, boolean ascending) {
        String orderBy = switch (sortKey == null ? "" : sortKey) {
            case "name" -> "name";
            case "createdAt" -> "created_at";
            default -> "id";
        };
        String direction = ascending ? "ASC" : "DESC";
        String sql = """
                SELECT id, name, email FROM users
                ORDER BY %s %s, id ASC
                """.formatted(orderBy, direction);
        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    public List<User> findByIds(List<String> userIds) {
        Objects.requireNonNull(userIds, "userIds");
        userIds.forEach(id -> Objects.requireNonNull(id, "id"));
        if (userIds.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT id, name, email FROM users
                WHERE id IN (:ids) ORDER BY id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("ids", userIds);
        return namedParameterJdbcTemplate.query(sql, params, USER_MAPPER);
    }
}
