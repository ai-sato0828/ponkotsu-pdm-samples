package info.syachiku.audit;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity(name = "PurchaseOrder")
@Table(name = "orders")
public class Order {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {
    }

    Order(Long id, Customer customer, BigDecimal totalAmount) {
        this.id = id;
        this.customer = customer;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.WAITING;
    }

    Long getId() {
        return id;
    }

    Customer getCustomer() {
        return customer;
    }

    BigDecimal getTotalAmount() {
        return totalAmount;
    }

    OrderStatus getStatus() {
        return status;
    }

    void ship() {
        status = OrderStatus.SHIPPED;
    }
}
