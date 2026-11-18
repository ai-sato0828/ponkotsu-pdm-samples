package example.readonly;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReadOnlyRequiredTest {
    @Autowired OrderFacade facade;
    @Autowired OrderWriter writer;
    @Autowired DataSource dataSource;

    @BeforeEach
    void resetToTheSameCommittedOrder() throws Exception {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getAutoCommit());
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("delete from sample_orders");
                statement.executeUpdate("insert into sample_orders(id, status) values(1, 'WAITING')");
            }
        }
    }

    @Test
    void requiredWriterJoinsTheOuterReadOnlyTransaction() throws Exception {
        var observed = facade.readOnlyShip(1L);
        assertTrue(observed.txReadOnly());
        assertEquals("MANUAL", observed.flushMode());
        assertTrue(observed.entityReadOnly());
        assertManagedChange(observed);
        String database = reloadAfterTransaction();
        assertEquals("WAITING", database);
        print("outer-readOnly", observed, database);
    }

    @Test
    void theSameWriterPersistsInsideAWritableOuterTransaction() throws Exception {
        var observed = facade.writeShip(1L);
        assertWritable(observed);
        String database = reloadAfterTransaction();
        assertEquals("SHIPPED", database);
        print("outer-write", observed, database);
    }

    @Test
    void writerWithoutAnOuterTransactionStartsAWritableTransaction() throws Exception {
        var observed = writer.ship(1L);
        assertWritable(observed);
        String database = reloadAfterTransaction();
        assertEquals("SHIPPED", database);
        print("standalone-writer", observed, database);
    }

    private static void assertManagedChange(OrderWriter.Observation observed) {
        assertTrue(observed.managed());
        assertEquals("WAITING", observed.before());
        assertEquals("SHIPPED", observed.inMemory());
    }

    private static void assertWritable(OrderWriter.Observation observed) {
        assertFalse(observed.txReadOnly());
        assertEquals("AUTO", observed.flushMode());
        assertFalse(observed.entityReadOnly());
        assertManagedChange(observed);
    }

    private String reloadAfterTransaction() throws Exception {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        // 外側メソッドのトランザクション終了後、JPAの一次キャッシュを通さず読む。
        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select status from sample_orders where id=?")) {
            assertTrue(connection.getAutoCommit());
            statement.setLong(1, 1L);
            try (var rows = statement.executeQuery()) {
                assertTrue(rows.next());
                String status = rows.getString(1);
                assertFalse(rows.next());
                return status;
            }
        }
    }

    private static void print(String path, OrderWriter.Observation observed, String database) {
        System.out.printf("TX path=%s readOnly=%s flush=%s entityReadOnly=%s managed=%s memory=%s->%s database=%s%n",
                path, observed.txReadOnly(), observed.flushMode(), observed.entityReadOnly(),
                observed.managed(), observed.before(), observed.inMemory(), database);
    }
}
