package example.readonly;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sample_orders")
class PurchaseOrder {
    @Id
    private Long id;
    private String status;

    protected PurchaseOrder() {}

    String status() { return status; }
    void ship() { status = "SHIPPED"; }
}
