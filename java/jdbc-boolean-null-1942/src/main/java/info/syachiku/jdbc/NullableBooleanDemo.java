package info.syachiku.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class NullableBooleanDemo {
    private NullableBooleanDemo() {
    }

    public static void main(String[] args) throws SQLException {
        for (Reading row : observe()) {
            System.out.printf("id=%d getBoolean=%s wasNull=%s afterGetInt=%s getObject=%s restored=%s%n",
                    row.id(), row.primitive(), row.wasNullForActive(), row.wasNullAfterId(),
                    row.typed(), row.restored());
        }
    }

    static List<Reading> observe() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE feature_flags (id INT PRIMARY KEY, active BOOLEAN)");
            statement.executeUpdate("INSERT INTO feature_flags VALUES (1, TRUE), (2, FALSE), (3, NULL)");

            List<Reading> readings = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery("SELECT id, active FROM feature_flags ORDER BY id")) {
                while (rows.next()) {
                    boolean primitive = rows.getBoolean("active");
                    boolean wasNullForActive = rows.wasNull();
                    int id = rows.getInt("id");
                    boolean wasNullAfterId = rows.wasNull();
                    Boolean typed = rows.getObject("active", Boolean.class);

                    readings.add(new Reading(id, primitive, wasNullForActive, wasNullAfterId, typed));
                }
            }
            return readings;
        }
    }

    record Reading(int id, boolean primitive, boolean wasNullForActive, boolean wasNullAfterId, Boolean typed) {
        Boolean restored() {
            return wasNullForActive ? null : Boolean.valueOf(primitive);
        }
    }
}
