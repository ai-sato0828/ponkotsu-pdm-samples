package info.syachiku.embedded.correct;

import info.syachiku.embedded.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_accounts")
public class CustomerAccount extends AuditableEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    protected CustomerAccount() {}

    public CustomerAccount(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
