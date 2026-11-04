package info.syachiku.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "incoming_event")
public class IncomingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, String> payload;

    protected IncomingEvent() {
    }

    public IncomingEvent(Map<String, String> payload) {
        this.payload = Map.copyOf(payload);
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getPayload() {
        return Map.copyOf(payload);
    }
}
