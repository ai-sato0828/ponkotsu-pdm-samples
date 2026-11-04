package info.syachiku.embedded.correct;

import info.syachiku.embedded.model.Address;
import info.syachiku.embedded.model.AuditableEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends AuditableEntity {
    @Id
    private Long id;

    @Embedded
    @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code", nullable = false, length = 8))
    @AttributeOverride(name = "city", column = @Column(name = "billing_city", nullable = false, length = 80))
    @AttributeOverride(name = "street", column = @Column(name = "billing_street", nullable = false, length = 160))
    private Address billingAddress;

    @Embedded
    @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", nullable = false, length = 8))
    @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false, length = 80))
    @AttributeOverride(name = "street", column = @Column(name = "shipping_street", nullable = false, length = 160))
    private Address shippingAddress;

    protected PurchaseOrder() {}

    public PurchaseOrder(Long id, Address billingAddress, Address shippingAddress) {
        this.id = id;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }

    public Address billingAddress() {
        return billingAddress;
    }

    public Address shippingAddress() {
        return shippingAddress;
    }
}
