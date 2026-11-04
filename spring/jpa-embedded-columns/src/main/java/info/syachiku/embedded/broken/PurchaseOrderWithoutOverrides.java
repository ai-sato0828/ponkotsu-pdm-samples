package info.syachiku.embedded.broken;

import info.syachiku.embedded.model.Address;
import info.syachiku.embedded.model.AuditableEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrderWithoutOverrides extends AuditableEntity {
    @Id
    private Long id;

    @Embedded
    private Address billingAddress;

    @Embedded
    private Address shippingAddress;

    protected PurchaseOrderWithoutOverrides() {}
}
