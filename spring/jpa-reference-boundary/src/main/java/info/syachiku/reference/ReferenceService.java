package info.syachiku.reference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceService {
    private final CustomerRepository repository;
    private final EntityManagerFactory entityManagerFactory;
    @PersistenceContext private EntityManager entityManager;

    ReferenceService(CustomerRepository repository, EntityManagerFactory entityManagerFactory) {
        this.repository = repository;
        this.entityManagerFactory = entityManagerFactory;
    }

    public record FindResult(boolean entityPresent, Optional<String> name, long afterFind, long afterName) {}
    public record DetachedReference(Customer reference, long sql, boolean initialized) {}
    public record ReuseResult(boolean sameReference, long afterFind, long afterReference,
                              long afterName, String name) {}

    // BEGIN findSnapshot
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public FindResult findSnapshot(long id) {
        Statistics statistics = resetMeasurement();
        Optional<Customer> found = repository.findById(id);
        long afterFind = statistics.getPrepareStatementCount();
        Optional<String> name = found.map(Customer::getName);
        return new FindResult(found.isPresent(), name, afterFind, statistics.getPrepareStatementCount());
    }
    // END findSnapshot

    // BEGIN referenceStages
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void referenceInsideTransaction(long id, ReferenceTrace trace) {
        Statistics statistics = resetMeasurement();
        String step = "getReferenceById";
        try {
            Customer reference = repository.getReferenceById(id);
            trace.referenceSql = statistics.getPrepareStatementCount();
            trace.initializedAfterReference = Hibernate.isInitialized(reference);
            step = "getId";
            trace.id = reference.getId();
            trace.idSql = statistics.getPrepareStatementCount();
            trace.initializedAfterId = Hibernate.isInitialized(reference);
            step = "getName";
            trace.name = reference.getName();
            trace.nameSql = statistics.getPrepareStatementCount();
            trace.initializedAfterName = Hibernate.isInitialized(reference);
        } catch (RuntimeException failure) {
            trace.failedAt = step;
            trace.failedSql = statistics.getPrepareStatementCount();
            trace.failureClass = failure.getClass().getName();
            throw failure;
        }
    }
    // END referenceStages

    // BEGIN referenceOutside
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public DetachedReference referenceForOutsideAccess(long id) {
        Statistics statistics = resetMeasurement();
        Customer reference = repository.getReferenceById(id);
        return new DetachedReference(reference, statistics.getPrepareStatementCount(),
                Hibernate.isInitialized(reference));
    }
    // END referenceOutside

    // BEGIN unsafeOptional
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public String findThenUnsafeOptionalGet(long id) {
        resetMeasurement();
        return repository.findById(id).get().getName();
    }
    // END unsafeOptional

    // BEGIN alreadyLoaded
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ReuseResult reuseInsideOneContext(long id) {
        Statistics statistics = resetMeasurement();
        Customer found = repository.findById(id).orElseThrow();
        long afterFind = statistics.getPrepareStatementCount();
        Customer reference = repository.getReferenceById(id);
        long afterReference = statistics.getPrepareStatementCount();
        String name = reference.getName();
        return new ReuseResult(found == reference, afterFind, afterReference,
                statistics.getPrepareStatementCount(), name);
    }
    // END alreadyLoaded

    private Statistics resetMeasurement() {
        entityManager.clear();
        entityManagerFactory.getCache().evictAll();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        if (!statistics.isStatisticsEnabled()) {
            throw new IllegalStateException("Hibernate statistics must be enabled");
        }
        statistics.clear();
        SqlCapture.begin();
        return statistics;
    }
}
