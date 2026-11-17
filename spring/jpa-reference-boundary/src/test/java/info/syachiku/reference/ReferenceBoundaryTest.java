package info.syachiku.reference;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
class ReferenceBoundaryTest {
    @Autowired CustomerRepository repository;
    @Autowired ReferenceService service;
    @Autowired CustomerQueryService queryService;
    @Autowired EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void commitSeedBeforeOpeningTheMeasurementContext() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        repository.deleteAll();
        repository.saveAndFlush(new Customer(1L, "田中"));
        entityManagerFactory.getCache().evictAll();
    }

    @Test @Order(1)
    void findExistingRowReturnsPresentAfterOneSelect() {
        var result = service.findSnapshot(1L);
        assertTrue(result.entityPresent());
        assertEquals(Optional.of("田中"), result.name());
        assertEquals(1, result.afterFind());
        assertEquals(1, result.afterName());
        assertSelects(1);
        System.out.println("FIND id=1 present=true sql=1/1 name=田中");
    }

    @Test @Order(2)
    void findMissingRowReturnsEmptyAfterOneSelect() {
        var result = service.findSnapshot(999L);
        assertFalse(result.entityPresent());
        assertTrue(result.name().isEmpty());
        assertEquals(1, result.afterFind());
        assertEquals(1, result.afterName());
        assertSelects(1);
        System.out.println("FIND id=999 present=false sql=1/1");
    }

    @Test @Order(3)
    void referenceExistingRowLoadsOnTheNonIdProperty() {
        ReferenceTrace trace = new ReferenceTrace();
        service.referenceInsideTransaction(1L, trace);
        assertEquals(0, trace.referenceSql);
        assertEquals(0, trace.idSql);
        assertEquals(1, trace.nameSql);
        assertEquals(1L, trace.id);
        assertEquals("田中", trace.name);
        assertEquals(Boolean.FALSE, trace.initializedAfterReference);
        assertEquals(Boolean.FALSE, trace.initializedAfterId);
        assertEquals(Boolean.TRUE, trace.initializedAfterName);
        assertNull(trace.failedAt);
        assertSelects(1);
        System.out.println("REFERENCE id=1 sql=0/0/1 initialized=false/false/true name=田中");
    }

    @Test @Order(4)
    void referenceMissingRowFailsAtTheNonIdPropertyInsideTheTransaction() {
        ReferenceTrace trace = new ReferenceTrace();
        assertThrows(EntityNotFoundException.class,
                () -> service.referenceInsideTransaction(999L, trace));
        assertEquals(0, trace.referenceSql);
        assertEquals(0, trace.idSql);
        assertEquals(999L, trace.id);
        assertEquals(1, trace.failedSql);
        assertEquals("getName", trace.failedAt);
        assertEquals(EntityNotFoundException.class.getName(), trace.failureClass);
        assertEquals(Boolean.FALSE, trace.initializedAfterReference);
        assertEquals(Boolean.FALSE, trace.initializedAfterId);
        assertNull(trace.initializedAfterName);
        assertEquals(-1, trace.nameSql);
        assertSelects(1);
        System.out.println("REFERENCE id=999 sql=0/0/fail:1 failedAt=getName exception=EntityNotFoundException");
    }

    @Test @Order(5)
    void obtainingAnIdFromAReferenceDoesNotProveTheRowExists() {
        var observed = service.referenceForOutsideAccess(999L);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(0, observed.sql());
        assertFalse(observed.initialized());
        assertEquals(999L, observed.reference().getId());
        assertFalse(Hibernate.isInitialized(observed.reference()));
        assertEquals(0, statements());
        assertSelects(0);
        System.out.println("ID_ONLY missingId=999 returnedId=999 sql=0 existsConfirmed=false");
    }

    @Test @Order(6)
    void accessingAnExistingReferenceAfterTheTransactionIsALazyContextProblem() {
        var observed = service.referenceForOutsideAccess(1L);
        assertEquals(0, observed.sql());
        assertFalse(observed.initialized());
        assertEquals(1L, observed.reference().getId());
        assertThrows(LazyInitializationException.class, () -> observed.reference().getName());
        assertEquals(0, statements());
        assertSelects(0);
        System.out.println("OUTSIDE_TX existingId=1 exception=LazyInitializationException sql=0");
    }

    @Test @Order(7)
    void optionalGetOnAMissingFindResultIsNotEntityNotFoundException() {
        assertThrows(NoSuchElementException.class, () -> service.findThenUnsafeOptionalGet(999L));
        assertEquals(1, statements());
        assertSelects(1);
        System.out.println("OPTIONAL_GET missingId=999 exception=NoSuchElementException sql=1");
    }

    @Test @Order(8)
    void anAlreadyLoadedManagedEntityChangesTheReferenceObservation() {
        var observed = service.reuseInsideOneContext(1L);
        assertEquals(new ReferenceService.ReuseResult(true, 1, 1, 1, "田中"), observed);
        assertSelects(1);
        System.out.println("REUSED_CONTEXT id=1 same=true cumulativeSql=1/1/1");
    }

    @Test @Order(9)
    void separateServiceTransactionsRepeatTheFreshContextObservation() {
        ReferenceTrace first = new ReferenceTrace();
        ReferenceTrace second = new ReferenceTrace();
        service.referenceInsideTransaction(1L, first);
        assertSelects(1);
        service.referenceInsideTransaction(1L, second);
        assertSelects(1);
        assertEquals(0, first.referenceSql);
        assertEquals(1, first.nameSql);
        assertEquals(0, second.referenceSql);
        assertEquals(1, second.nameSql);
        assertEquals(Boolean.FALSE, second.initializedAfterReference);
        System.out.println("FRESH_CONTEXT twice referenceSql=0/0 nameSql=1/1");
    }

    @Test @Order(10)
    void requiredCustomerReturnsNameForAnExistingEntity() {
        assertEquals("田中", queryService.requireCustomerName(1L));
        System.out.println("REQUIRE id=1 exists=true name=田中");
    }

    @Test @Order(11)
    void requiredCustomerUsesAnExplicitBusinessExceptionForAMissingEntity() {
        var failure = assertThrows(CustomerNotFoundException.class,
                () -> queryService.requireCustomerName(999L));
        assertEquals("Customer not found: 999", failure.getMessage());
        System.out.println("REQUIRE id=999 exception=CustomerNotFoundException");
    }

    @Test @Order(12)
    void existingEntityWithNullNameIsNotAMissingEntity() {
        repository.saveAndFlush(new Customer(2L, null));
        var snapshot = service.findSnapshot(2L);
        assertTrue(snapshot.entityPresent());
        assertTrue(snapshot.name().isEmpty());
        assertEquals(1, snapshot.afterFind());
        assertEquals(1, snapshot.afterName());
        assertSelects(1);
        assertNull(queryService.requireCustomerName(2L));
        System.out.println("NULL_NAME id=2 entityPresent=true nameOptionalEmpty=true requiredName=null");
    }

    private void assertSelects(int expected) {
        var sql = SqlCapture.snapshot();
        assertEquals(expected, sql.size());
        sql.forEach(statement -> assertTrue(
                statement.stripLeading().toLowerCase(Locale.ROOT).startsWith("select")));
        System.out.printf("SQL_WINDOW count=%d statements=%s%n", expected, sql);
    }

    private long statements() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics()
                .getPrepareStatementCount();
    }
}
