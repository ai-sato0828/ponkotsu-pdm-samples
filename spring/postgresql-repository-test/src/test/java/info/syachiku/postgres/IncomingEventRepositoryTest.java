package info.syachiku.postgres;

import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=always",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class IncomingEventRepositoryTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres"
            + "@sha256:57c72fd2a128e416c7fcc499958864df5301e940bca0a56f58fddf30ffc07777");

    @Autowired
    private IncomingEventRepository repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readyだけが一致しdoneとキーなしは除外される() {
        var ready = new IncomingEvent(Map.of("status", "READY", "source", "api"));
        var done = new IncomingEvent(Map.of("status", "DONE"));
        var missing = new IncomingEvent(Map.of("source", "batch"));
        repository.saveAllAndFlush(List.of(ready, done, missing));
        entityManager.clear();

        assertThat(repository.findByPayloadStatus("READY"))
                .extracting(IncomingEvent::getId)
                .containsExactly(ready.getId());
        assertThat(repository.findByPayloadStatus("DONE"))
                .extracting(IncomingEvent::getId)
                .containsExactly(done.getId());
    }

    @Test
    void 一致しないステータスは空リストを返す() {
        repository.saveAndFlush(new IncomingEvent(Map.of("status", "READY")));

        assertThat(repository.findByPayloadStatus("QUEUED")).isEmpty();
    }

    @Test
    void 接続先がPostgreSQLでDDLの列がjsonbである() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
        var event = repository.saveAndFlush(new IncomingEvent(Map.of("status", "READY")));

        assertThat(jdbc.queryForObject("show server_version_num", String.class)).isEqualTo("160014");
        assertThat(jdbc.queryForObject(
                "select pg_typeof(payload)::text from incoming_event where id = ?",
                String.class, event.getId())).isEqualTo("jsonb");
    }
}
