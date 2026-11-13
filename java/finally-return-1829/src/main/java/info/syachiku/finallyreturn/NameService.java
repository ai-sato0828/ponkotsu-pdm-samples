package info.syachiku.finallyreturn;

import java.util.Objects;

public final class NameService {
    private final NameRepository repository;
    private final RecordingAudit audit;

    public NameService(NameRepository repository, RecordingAudit audit) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.audit = Objects.requireNonNull(audit, "audit");
    }

    public String loadNameBroken() {
        try {
            return repository.loadName();
        } finally {
            audit.record();
            return "guest";
        }
    }

    public String loadName() {
        try {
            return Objects.requireNonNull(repository.loadName(), "repository returned null");
        } finally {
            audit.record();
        }
    }
}
