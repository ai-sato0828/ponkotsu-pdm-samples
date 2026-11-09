package info.syachiku.sqlbinding;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class ComparisonRun {
    public static void main(String[] args) {
        DataSource database = SampleDatabase.create();
        UserRepository safe = new UserRepository(database);
        UnsafeUserRepository unsafe = new UnsafeUserRepository(database);
        for (String input : List.of("Alice", "O'Brien", "' OR '1'='1")) {
            String bad;
            try {
                bad = UserRepositoryTest.ids(unsafe.findByName(input)).toString();
            } catch (DataAccessException ex) {
                bad = ex.getClass().getSimpleName();
            }
            System.out.println("input=" + input + " concat=" + bad
                    + " bind=" + UserRepositoryTest.ids(safe.findByName(input)));
        }
        try {
            List<User> sorted = new org.springframework.jdbc.core.JdbcTemplate(database)
                    .query("SELECT id, name, email FROM users ORDER BY ?", UserRepository.USER_MAPPER, "name");
            System.out.println("ORDER BY ? bound name => " + UserRepositoryTest.ids(sorted));
        } catch (DataAccessException ex) {
            System.out.println("ORDER BY ? bound name => " + ex.getClass().getSimpleName());
        }
    }
}
