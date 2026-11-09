package info.syachiku.sqlbinding;

import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public final class SampleDatabase {
    private SampleDatabase() {
    }

    public static DataSource create() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:sample_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        dataSource.setDriverClassName("org.h2.Driver");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql"),
                new ClassPathResource("data.sql")).execute(dataSource);
        return dataSource;
    }
}
