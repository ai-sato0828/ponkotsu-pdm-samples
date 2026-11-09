package info.syachiku.sqlbinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

public class PlainJdbcUserRepository {
    private final DataSource dataSource;

    public PlainJdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<User> findByName(String name) throws SQLException {
        Objects.requireNonNull(name, "name");
        String sql = """
                SELECT id, name, email FROM users
                WHERE name = ? ORDER BY id
                """;
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(new User(resultSet.getString("id"),
                            resultSet.getString("name"), resultSet.getString("email")));
                }
            }
        }
        return users;
    }
}
