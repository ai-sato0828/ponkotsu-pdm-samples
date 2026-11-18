package example.postal.fixed;

import example.postal.PostalCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postal_addresses")
public class Address {
    @Id private Long id;

    // BEGIN convertedField
    @Convert(converter = PostalCodeConverter.class)
    @Column(name = "postal_code", length = 7)
    private PostalCode postalCode;
    // END convertedField

    protected Address() {}

    public Address(Long id, PostalCode postalCode) {
        this.id = id;
        this.postalCode = postalCode;
    }

    public Long getId() { return id; }
    public PostalCode getPostalCode() { return postalCode; }
}
