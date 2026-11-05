package info.syachiku.fetchpaging;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderPageTest {
    @Autowired
    private OrderRepository orders;
    @Autowired
    private OrderPageService service;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private TransactionTemplate transaction;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        transaction.executeWithoutResult(status -> {
            entityManager.createQuery("delete from OrderItem").executeUpdate();
            entityManager.createQuery("delete from PurchaseOrder").executeUpdate();
            var first = new Order(3L, LocalDate.of(2026, 11, 3));
            first.addItem(31L, "A");
            first.addItem(32L, "B");
            first.addItem(33L, "C");
            var second = new Order(1L, LocalDate.of(2026, 11, 2));
            var third = new Order(2L, LocalDate.of(2026, 11, 2));
            third.addItem(21L, "D");
            entityManager.persist(first);
            entityManager.persist(second);
            entityManager.persist(third);
        });
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    void strictSettingRejectsPagingOverCollectionFetch() {
        var error = assertThrows(DataAccessException.class,
                () -> orders.findPageWithItems(PageRequest.of(0, 2)));
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(error);
        assertInstanceOf(HibernateException.class, cause);
        assertTrue(cause.getMessage().contains("fail_on_pagination_over_collection_fetch"));
        System.out.println("直接FETCH: " + cause.getMessage());
    }

    @Test
    void firstPageKeepsTwoOrdersAndTheirCompleteItemsInPageOrder() {
        assertEquals(List.of(new OrderPageService.OrderRow(3L, List.of("A", "B", "C")),
                new OrderPageService.OrderRow(1L, List.of())), service.findPage(0, 2));
        assertReadStatements(2, "page=0: 注文3[A, B, C]・注文1[]");
    }

    @Test
    void secondPageKeepsRemainingOrderAndItsItems() {
        assertEquals(List.of(new OrderPageService.OrderRow(2L, List.of("D"))), service.findPage(1, 2));
        assertReadStatements(2, "page=1: 注文2[D]");
    }

    @Test
    void emptyPageSkipsTheFetchQuery() {
        assertEquals(List.of(), service.findPage(2, 2));
        assertReadStatements(1, "page=2: []");
    }

    private void assertReadStatements(long expected, String description) {
        assertEquals(expected, statistics.getPrepareStatementCount());
        System.out.printf("%s / SELECT=%d%n", description, expected);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(OrderPageService.class)
    static class TestApplication {
    }
}
