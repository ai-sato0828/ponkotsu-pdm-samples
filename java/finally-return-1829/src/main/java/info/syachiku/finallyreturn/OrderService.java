package info.syachiku.finallyreturn;

import java.util.Objects;

public final class OrderService {
    private final RecordingOrderRepository repository;
    private final RecordingAudit audit;

    public OrderService(RecordingOrderRepository repository, RecordingAudit audit) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.audit = Objects.requireNonNull(audit, "audit");
    }

    public boolean updateOrderBroken() {
        try {
            repository.save();
            return true;
        } finally {
            audit.record();
            return false;
        }
    }

    public boolean updateOrder() {
        try {
            repository.save();
            return true;
        } finally {
            audit.record();
        }
    }
}
