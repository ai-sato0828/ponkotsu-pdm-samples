package info.syachiku.audit;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Customer() {
    }

    Customer(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    String getName() {
        return name;
    }
}
