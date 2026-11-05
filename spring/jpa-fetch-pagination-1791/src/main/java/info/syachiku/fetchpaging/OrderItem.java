package info.syachiku.fetchpaging;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
class OrderItem {
    @Id
    private Long id;
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {
    }

    OrderItem(long id, Order order, String sku) {
        this.id = id;
        this.order = order;
        this.sku = sku;
    }

    String sku() {
        return sku;
    }
}
