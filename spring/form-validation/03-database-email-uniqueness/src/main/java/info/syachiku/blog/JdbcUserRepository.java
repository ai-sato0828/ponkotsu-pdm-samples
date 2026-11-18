package info.syachiku.blog;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jdbc.queryForObject("select count(*) from registration_users where email = ?",
                Integer.class, email) > 0;
    }

    // BEGIN insert
    @Override
    public long save(UserRegisterCommand command) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into registration_users (name,email,department_code) values (?,?,?)",
                    new String[]{"id"});
            statement.setString(1, command.name());
            statement.setString(2, command.email());
            statement.setString(3, command.departmentCode());
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }
    // END insert
}
