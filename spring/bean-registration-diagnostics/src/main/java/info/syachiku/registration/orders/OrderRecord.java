package info.syachiku.registration.orders;

import jakarta.persistence.*;

@Entity
@Table(name = "registered_order")
public class OrderRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false)
    private String status;

    protected OrderRecord() {
    }

    OrderRecord(OrderRow row) {
        reference = row.reference();
        status = row.status();
    }

    Long getId() {
        return id;
    }
}
