package example.postal;

import example.postal.fixed.Address;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.sql.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PostalApplication.class)
class PostalPersistenceTest {
    @Autowired EntityManagerFactory emf;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanOutsideAnyTestTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        String url = jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<String>)
                connection -> connection.getMetaData().getURL());
        assertTrue(url.startsWith("jdbc:h2:mem:converted-postal"));
        jdbc.update("delete from postal_addresses");
        emf.getCache().evictAll();
    }

    // BEGIN roundtrip
    @Test
    void committedTextColumnRestoresTheValueInADifferentEntityManager() {
        Address original = new Address(1L, new PostalCode("0123456"));
        EntityManager writer = emf.createEntityManager();
        try {
            writer.getTransaction().begin();
            writer.persist(original);
            writer.getTransaction().commit();
        } finally {
            close(writer);
        }
        assertFalse(writer.isOpen());
        String raw = jdbc.query("select postal_code from postal_addresses where id=1", rs -> {
            assertTrue(rs.next());
            assertEquals(Types.VARCHAR, rs.getMetaData().getColumnType(1));
            assertEquals(7, rs.getMetaData().getPrecision(1));
            return rs.getString(1);
        });
        assertEquals("0123456", raw);
        EntityManager reader = emf.createEntityManager();
        assertNotSame(writer, reader);
        try {
            reader.getTransaction().begin();
            Address restored = reader.find(Address.class, 1L);
            assertNotSame(original, restored);
            assertEquals(original.getPostalCode(), restored.getPostalCode());
            assertEquals("0123456", restored.getPostalCode().value());
            reader.getTransaction().commit();
        } finally {
            close(reader);
        }
        System.out.println("ROUNDTRIP column=0123456 jdbcType=VARCHAR length=7 restored=0123456 writerClosed=true freshReader=true");
    }

    // END roundtrip

    @Test
    void aNullOptionalFieldPersistsAndRestoresAsNull() {
        EntityManager writer = emf.createEntityManager();
        try {
            writer.getTransaction().begin();
            writer.persist(new Address(2L, null));
            writer.getTransaction().commit();
        } finally { close(writer); }
        assertNull(jdbc.queryForObject("select postal_code from postal_addresses where id=2", String.class));
        EntityManager reader = emf.createEntityManager();
        try {
            reader.getTransaction().begin();
            assertNull(reader.find(Address.class, 2L).getPostalCode());
            reader.getTransaction().commit();
        } finally { close(reader); }
        System.out.println("NULL_FIELD sqlNull=true restoredNull=true");
    }

    @Test
    void invalidStoredTextIsRejectedAtTheConversionBoundary() {
        jdbc.update("insert into postal_addresses(id,postal_code) values (?,?)", 3L, "12A4567");
        assertEquals("12A4567", jdbc.queryForObject("select postal_code from postal_addresses where id=3", String.class));
        EntityManager reader = emf.createEntityManager();
        try {
            reader.getTransaction().begin();
            var failure = assertThrows(PersistenceException.class, () -> reader.find(Address.class, 3L));
            Throwable root = failure;
            while (root.getCause() != null) root = root.getCause();
            assertInstanceOf(IllegalArgumentException.class, root);
            assertEquals("郵便番号はASCII数字7桁で指定してください", root.getMessage());
            System.out.printf("INVALID_STORED column=12A4567 outer=%s root=%s%n",
                    failure.getClass().getSimpleName(), root.getClass().getSimpleName());
        } finally { close(reader); }
    }

    private void close(EntityManager manager) {
        if (manager.getTransaction().isActive()) manager.getTransaction().rollback();
        manager.close();
    }
}
