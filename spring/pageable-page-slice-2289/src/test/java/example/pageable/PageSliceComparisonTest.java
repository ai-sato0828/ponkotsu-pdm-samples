package example.pageable;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import static example.pageable.Inquiry.Status.CLOSED;
import static example.pageable.Inquiry.Status.OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false, properties =
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=example.pageable.SqlCapture")
class PageSliceComparisonTest {
    private static final Pattern SELECT = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern COUNT = Pattern.compile("^\\s*select\\s+count\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Sort ORDER = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    @Autowired
    private InquiryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void seedRowsBeforeRecordingSql() {
        Instant at = Instant.parse("2026-11-01T00:00:00Z");
        entityManager.persist(new Inquiry(1, OPEN, at));
        entityManager.persist(new Inquiry(2, OPEN, at));
        entityManager.persist(new Inquiry(3, OPEN, at));
        entityManager.persist(new Inquiry(4, CLOSED, at));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void fullFirstPageNeedsAnExtraCountButSliceDoesNot() {
        Pageable first = PageRequest.of(0, 2, ORDER);
        var page = SqlCapture.capture(() -> repository.findByStatus(OPEN, first));
        var slice = SqlCapture.capture(() -> repository.findSliceByStatus(OPEN, first));

        assertEquals(List.of(3L, 2L), ids(page.result()));
        assertEquals(ids(page.result()), ids(slice.result()));
        assertTrue(page.result().hasNext());
        assertTrue(slice.result().hasNext());
        assertEquals(3, page.result().getTotalElements());
        assertSql(page.statements(), 2, 1);
        assertSql(slice.statements(), 1, 0);

        print("PAGE first", page.result(), page.result().getTotalElements(), page.statements());
        print("SLICE first", slice.result(), "なし", slice.statements());
    }

    @Test
    void partialLastPageCanInferItsTotalWithoutCounting() {
        Pageable last = PageRequest.of(1, 2, ORDER);
        var page = SqlCapture.capture(() -> repository.findByStatus(OPEN, last));

        assertEquals(List.of(1L), ids(page.result()));
        assertFalse(page.result().hasNext());
        assertEquals(3, page.result().getTotalElements());
        assertSql(page.statements(), 1, 0);

        print("PAGE last", page.result(), page.result().getTotalElements(), page.statements());
    }

    private static List<Long> ids(Slice<Inquiry> result) {
        return result.getContent().stream().map(Inquiry::id).toList();
    }

    private static long countByPattern(List<String> sql, Pattern pattern) {
        return sql.stream().filter(statement -> pattern.matcher(statement).find()).count();
    }

    private static void assertSql(List<String> sql, int selects, int counts) {
        assertEquals(selects, sql.size());
        assertEquals(selects, countByPattern(sql, SELECT));
        assertEquals(counts, countByPattern(sql, COUNT));
    }

    private static void print(String label, Slice<Inquiry> result, Object total, List<String> sql) {
        System.out.printf("%s ids=%s hasNext=%s totalElements=%s select=%d count=%d%n",
                label, ids(result), result.hasNext(), total,
                countByPattern(sql, SELECT), countByPattern(sql, COUNT));
        sql.forEach(statement -> System.out.println("  SQL " + statement));
    }
}
