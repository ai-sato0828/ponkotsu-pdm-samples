package example.shipping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
class ShippingNote {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 200)
    private String memo;

    protected ShippingNote() {
    }

    ShippingNote(String memo) {
        this.memo = memo;
    }

    Long getId() {
        return id;
    }

    Long getVersion() {
        return version;
    }

    String getMemo() {
        return memo;
    }

    void changeMemo(String newMemo) {
        memo = newMemo;
    }
}
