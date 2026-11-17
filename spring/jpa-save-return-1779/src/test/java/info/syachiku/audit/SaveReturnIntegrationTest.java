package info.syachiku.audit;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SaveReturnIntegrationTest {
    @Autowired OrderRepository repository;
    @Autowired SaveReturnService service;
    @Autowired EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void commitTheWaitingFixtureBeforeEachServiceCall() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        repository.deleteAll();
        assertFalse(repository.existsById(1L));
        Order fixture = new Order(1L, null, BigDecimal.ONE);
        try (var entityManager = entityManagerFactory.createEntityManager()) {
            var information = JpaEntityInformationSupport.getEntityInformation(
                    Order.class, entityManager);
            assertFalse(information.isNew(fixture));
        }
        repository.saveAndFlush(fixture);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void changingTheDetachedOriginalAfterSaveDoesNotChangeTheDatabase() {
        Order detached = loadDetachedInASeparateRepositoryCall();
        var observed = service.changeDetachedOriginalAfterSave(detached);
        assertEquals(new SaveReturnService.Observation(false, false, true, false,
                OrderStatus.SHIPPED, OrderStatus.WAITING), observed);
        assertEquals(OrderStatus.SHIPPED, detached.getStatus());
        assertEquals(OrderStatus.WAITING, readStatusAfterServiceCommit());
        print("DETACHED_ORIGINAL", observed, OrderStatus.WAITING);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void changingTheManagedReturnValueIsCommittedWhileOriginalStaysDetached() {
        Order detached = loadDetachedInASeparateRepositoryCall();
        var observed = service.changeReturnedManagedAfterSave(detached);
        assertEquals(OrderStatus.SHIPPED, readStatusAfterServiceCommit());
        assertEquals(new SaveReturnService.Observation(false, false, true, false,
                OrderStatus.WAITING, OrderStatus.SHIPPED), observed);
        assertEquals(OrderStatus.WAITING, detached.getStatus());
        print("SAVE_RETURN", observed, OrderStatus.SHIPPED);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void savingAnAlreadyManagedOrderCanReturnTheSameReference() {
        var observed = service.saveAlreadyManagedAndShip(1L);
        assertEquals(new SaveReturnService.Observation(true, true, true, true,
                OrderStatus.SHIPPED, OrderStatus.SHIPPED), observed);
        assertEquals(OrderStatus.SHIPPED, readStatusAfterServiceCommit());
        print("ALREADY_MANAGED", observed, OrderStatus.SHIPPED);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void changingTheOriginalBeforeSaveCopiesItsCurrentStateAtMergeTime() {
        Order detached = loadDetachedInASeparateRepositoryCall();
        var observed = service.changeOriginalBeforeSave(detached);
        assertEquals(new SaveReturnService.Observation(false, false, true, false,
                OrderStatus.SHIPPED, OrderStatus.SHIPPED), observed);
        assertEquals(OrderStatus.SHIPPED, detached.getStatus());
        assertEquals(OrderStatus.SHIPPED, readStatusAfterServiceCommit());
        print("ORIGINAL_BEFORE_SAVE", observed, OrderStatus.SHIPPED);
    }

    private Order loadDetachedInASeparateRepositoryCall() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        Order detached = repository.findById(1L).orElseThrow();
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        return detached;
    }

    private OrderStatus readStatusAfterServiceCommit() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        return repository.findById(1L).orElseThrow().getStatus();
    }

    private static void print(String scenario, SaveReturnService.Observation value,
                              OrderStatus databaseStatus) {
        System.out.printf("CASE %s before=%s originalAfterSave=%s returnedAfterSave=%s same=%s originalStatus=%s returnedStatus=%s db=%s%n",
                scenario, value.originalManagedBeforeSave(), value.originalManagedAfterSave(),
                value.returnedManagedAfterSave(), value.sameReference(),
                value.originalStatusAfterChange(), value.returnedStatusAfterChange(), databaseStatus);
    }
}
