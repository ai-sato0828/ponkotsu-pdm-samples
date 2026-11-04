package info.syachiku.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import org.hibernate.SessionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OrderQueryServiceTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private TransactionTemplate transaction;
    @Autowired
    private OrderQueryService queries;

    @ParameterizedTest
    @CsvSource({"false, 4", "true, 2"})
    void 顧客の共有数でSELECT回数が変わり修正後は一回になる(
            boolean sameCustomer, long expectedSelects) {
        prepareOrders(sameCustomer);

        List<OrderRow> before = countStatements(queries::findRows, expectedSelects);
        assertEquals(List.of(1L, 2L, 3L), before.stream().map(OrderRow::orderId).toList());
        assertEquals(sameCustomer ? List.of("C1", "C1", "C1") : List.of("C1", "C2", "C3"),
                before.stream().map(OrderRow::customerName).toList());
        assertEquals(before, countStatements(queries::findRowsWithFetchJoin, 1));
        assertEquals(before, countStatements(queries::findRowsWithEntityGraph, 1));
        assertEquals(before, countStatements(queries::findRowsWithProjection, 1));
    }

    private List<OrderRow> countStatements(Supplier<List<OrderRow>> query, long expected) {
        var stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        List<OrderRow> result = query.get();
        // These read-only methods perform SELECTs only.
        assertEquals(expected, stats.getPrepareStatementCount());
        return result;
    }

    private void prepareOrders(boolean sameCustomer) {
        transaction.executeWithoutResult(status -> {
            entityManager.createQuery("delete from PurchaseOrder").executeUpdate();
            entityManager.createQuery("delete from Customer").executeUpdate();

            var first = new Customer(1L, "C1");
            entityManager.persist(first);
            for (long id = 1; id <= 3; id++) {
                var customer = first;
                if (!sameCustomer && id != 1) {
                    customer = new Customer(id, "C" + id);
                    entityManager.persist(customer);
                }
                entityManager.persist(new Order(id, customer, BigDecimal.valueOf(100 * id)));
            }
        });
    }
}
