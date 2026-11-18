package info.syachiku.lazy;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    private Long id;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<OrderLine> lines = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public Long getId() {
        return id;
    }

    public List<OrderLine> getLines() {
        return lines;
    }
}
