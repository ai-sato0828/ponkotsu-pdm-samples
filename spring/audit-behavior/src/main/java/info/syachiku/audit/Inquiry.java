package info.syachiku.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Inquiry {
    @Id
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;

    protected Inquiry() {
    }

    Inquiry(Long id, String title, Status status, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }
}
