package info.syachiku.fetchpaging;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "PurchaseOrder")
class Order {
    @Id
    private Long id;
    private LocalDate placedOn;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    Order(long id, LocalDate placedOn) {
        this.id = id;
        this.placedOn = placedOn;
    }

    void addItem(long id, String sku) {
        items.add(new OrderItem(id, this, sku));
    }

    long id() {
        return id;
    }

    List<String> itemSkus() {
        return items.stream().map(OrderItem::sku).toList();
    }
}
