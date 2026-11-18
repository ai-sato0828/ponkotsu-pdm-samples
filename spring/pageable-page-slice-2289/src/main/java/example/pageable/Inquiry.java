package example.pageable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
class Inquiry {
    enum Status { OPEN, CLOSED }

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant createdAt;

    protected Inquiry() {
    }

    Inquiry(long id, Status status, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    long id() {
        return id;
    }
}
