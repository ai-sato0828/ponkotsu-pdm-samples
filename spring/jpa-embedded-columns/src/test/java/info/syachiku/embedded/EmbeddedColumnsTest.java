package info.syachiku.embedded;

import info.syachiku.embedded.broken.PurchaseOrderWithoutOverrides;
import info.syachiku.embedded.correct.CustomerAccount;
import info.syachiku.embedded.correct.PurchaseOrder;
import info.syachiku.embedded.model.Address;
import info.syachiku.embedded.model.UsagePeriod;
import jakarta.persistence.EntityManagerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.hibernate.MappingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EmbeddedColumnsTest {
    private static final String[] ADDRESS_COLUMNS = {
            "BILLING_POSTAL_CODE", "BILLING_CITY", "BILLING_STREET",
            "SHIPPING_POSTAL_CODE", "SHIPPING_CITY", "SHIPPING_STREET"
    };

    @Test
    void twoAddressesWithoutOverridesDuplicateAColumnDuringJpaStartup() {
        context(BrokenMapping.class).run(context -> {
            assertThat(context).hasFailed();
            Throwable cause = context.getStartupFailure();
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            assertThat(cause)
                    .isInstanceOf(MappingException.class)
                    .hasMessageContaining("Column 'city' is duplicated in mapping for entity");
            System.out.println(cause.getClass().getName() + ": " + cause.getMessage());
        });
    }

    @Test
    void generatedSchemaUsesSixAddressColumnsAndInheritsAuditColumns() throws Exception {
        Path ddl = Path.of("target", "schema.sql").toAbsolutePath();
        Files.deleteIfExists(ddl);
        context(CorrectMapping.class).withPropertyValues(
                "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
                "spring.jpa.properties.jakarta.persistence.schema-generation.database.action=drop-and-create",
                "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=" + ddl.toUri(),
                "spring.jpa.properties.hibernate.format_sql=true"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            EntityManagerFactory factory = context.getBean(EntityManagerFactory.class);
            try (var em = factory.createEntityManager()) {
                assertThat(em.createNativeQuery("""
                        select table_name from information_schema.tables
                        where table_schema = 'PUBLIC' and table_type = 'BASE TABLE'
                        """, String.class).getResultList())
                        .containsExactlyInAnyOrder("PURCHASE_ORDERS", "CUSTOMER_ACCOUNTS");
                assertThat(em.createNativeQuery("""
                        select column_name from information_schema.columns
                        where table_schema = 'PUBLIC' and table_name = 'PURCHASE_ORDERS'
                        """, String.class).getResultList())
                        .contains("ID", "CREATED_AT", "UPDATED_AT")
                        .contains(ADDRESS_COLUMNS)
                        .hasSize(9);
                assertThat(em.createNativeQuery("""
                        select column_name from information_schema.columns
                        where table_schema = 'PUBLIC' and table_name = 'CUSTOMER_ACCOUNTS'
                        """, String.class).getResultList())
                        .containsExactlyInAnyOrder("ID", "NAME", "CREATED_AT", "UPDATED_AT");
            }
            assertThat(Files.readString(ddl))
                    .contains("create table purchase_orders", "create table customer_accounts")
                    .doesNotContain("create table auditable_entity", "create table address");
        });
    }

    @Test
    void twoDifferentAddressesAndAuditDatesSurviveCommitAndReload() {
        context(CorrectMapping.class).run(context -> {
            assertThat(context).hasNotFailed();
            var billing = new Address("100-0001", "千代田区", "請求通り1");
            var shipping = new Address("530-0001", "大阪市北区", "配送通り2");
            EntityManagerFactory factory = context.getBean(EntityManagerFactory.class);
            try (var writer = factory.createEntityManager()) {
                writer.getTransaction().begin();
                writer.persist(new PurchaseOrder(1L, billing, shipping));
                writer.persist(new CustomerAccount(10L, "利用者A"));
                writer.getTransaction().commit();
            }
            try (var reader = factory.createEntityManager()) {
                var order = reader.find(PurchaseOrder.class, 1L);
                var customer = reader.find(CustomerAccount.class, 10L);
                assertThat(order.billingAddress()).usingRecursiveComparison().isEqualTo(billing);
                assertThat(order.shippingAddress()).usingRecursiveComparison().isEqualTo(shipping);
                assertThat(order.createdAt()).isNotNull();
                assertThat(order.updatedAt()).isNotNull();
                assertThat(customer.createdAt()).isNotNull();
                assertThat(customer.updatedAt()).isNotNull();
            }
        });
    }

    @Test
    void usagePeriodIncludesBothBoundaryDates() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 3);
        var period = new UsagePeriod(start, end);
        assertThat(period.includes(start.minusDays(1))).isFalse();
        assertThat(period.includes(start)).isTrue();
        assertThat(period.includes(end)).isTrue();
        assertThat(period.includes(end.plusDays(1))).isFalse();
        assertThatIllegalArgumentException().isThrownBy(() -> new UsagePeriod(end, start));
    }

    private ApplicationContextRunner context(Class<?> entityScan) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class))
                .withUserConfiguration(entityScan)
                .withPropertyValues(
                        "spring.datasource.generate-unique-name=true",
                        "spring.jpa.hibernate.ddl-auto=create-drop");
    }

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = PurchaseOrderWithoutOverrides.class)
    static class BrokenMapping {}

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = PurchaseOrder.class)
    @EnableJpaAuditing
    static class CorrectMapping {}
}
