package info.syachiku.mapping;

import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingTest {
    @Test
    void MappedSuperclassの列は二つの子テーブルにあり親テーブルはない() {
        var config = config("mapped");
        config.addAnnotatedClass(ParentUserEntity.class);
        config.addAnnotatedClass(ChildEntity.class);
        config.addAnnotatedClass(ArchivedEntity.class);
        try (var factory = config.buildSessionFactory(); var session = factory.openSession()) {
            var tx = session.beginTransaction();
            session.persist(new ChildEntity("1", "2026-11-01", "2026-11-02"));
            session.persist(new ArchivedEntity("1", "2026-11-03", "2026-11-04"));
            tx.commit();

            assertEquals("2026-11-02", session.createNativeQuery(
                    "SELECT UPDATE_DATE FROM ACTIVE_USERS WHERE USER_ID = '1'").getSingleResult());
            assertEquals("2026-11-04", session.createNativeQuery(
                    "SELECT UPDATE_DATE FROM ARCHIVED_USERS WHERE USER_ID = '1'").getSingleResult());
            Number count = (Number) session.createNativeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME IN ('PARENTUSERENTITY', 'PARENT_USER_ENTITY')")
                    .getSingleResult();
            assertEquals(0, count.intValue());
        }
    }

    @Test
    void 親もEntityなら既定の単一テーブル継承になる() {
        var config = config("single");
        config.addAnnotatedClass(SingleTableParent.class);
        config.addAnnotatedClass(SingleTableChild.class);
        try (var factory = config.buildSessionFactory(); var session = factory.openSession()) {
            var tx = session.beginTransaction();
            session.persist(new SingleTableChild("1"));
            tx.commit();

            Number count = (Number) session.createNativeQuery("SELECT COUNT(*) FROM SHARED_USERS")
                    .getSingleResult();
            assertEquals(1, count.intValue());
        }
    }

    private Configuration config(String name) {
        return new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:" + name)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop");
    }
}
