package info.syachiku.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveReturnService {
    private final OrderRepository repository;
    @PersistenceContext private EntityManager entityManager;

    SaveReturnService(OrderRepository repository) {
        this.repository = repository;
    }

    public record Observation(boolean originalManagedBeforeSave,
                              boolean originalManagedAfterSave,
                              boolean returnedManagedAfterSave,
                              boolean sameReference,
                              OrderStatus originalStatusAfterChange,
                              OrderStatus returnedStatusAfterChange) {}

    // BEGIN detachedOriginal
    @Transactional
    public Observation changeDetachedOriginalAfterSave(Order original) {
        boolean before = entityManager.contains(original);
        Order saved = repository.save(original);
        boolean originalManaged = entityManager.contains(original);
        boolean returnedManaged = entityManager.contains(saved);
        original.ship();
        return new Observation(before, originalManaged, returnedManaged,
                original == saved, original.getStatus(), saved.getStatus());
    }
    // END detachedOriginal

    // BEGIN saveReturn
    @Transactional
    public Observation changeReturnedManagedAfterSave(Order original) {
        boolean before = entityManager.contains(original);
        Order saved = repository.save(original);
        boolean originalManaged = entityManager.contains(original);
        boolean returnedManaged = entityManager.contains(saved);
        saved.ship();
        return new Observation(before, originalManaged, returnedManaged,
                original == saved, original.getStatus(), saved.getStatus());
    }
    // END saveReturn

    // BEGIN beforeSave
    @Transactional
    public Observation changeOriginalBeforeSave(Order original) {
        boolean before = entityManager.contains(original);
        original.ship();
        Order saved = repository.save(original);
        boolean originalManaged = entityManager.contains(original);
        boolean returnedManaged = entityManager.contains(saved);
        return new Observation(before, originalManaged, returnedManaged,
                original == saved, original.getStatus(), saved.getStatus());
    }
    // END beforeSave

    // BEGIN alreadyManaged
    @Transactional
    public Observation saveAlreadyManagedAndShip(long orderId) {
        Order original = repository.findById(orderId).orElseThrow();
        boolean before = entityManager.contains(original);
        Order saved = repository.save(original);
        boolean originalManaged = entityManager.contains(original);
        boolean returnedManaged = entityManager.contains(saved);
        saved.ship();
        return new Observation(before, originalManaged, returnedManaged,
                original == saved, original.getStatus(), saved.getStatus());
    }
    // END alreadyManaged
}
