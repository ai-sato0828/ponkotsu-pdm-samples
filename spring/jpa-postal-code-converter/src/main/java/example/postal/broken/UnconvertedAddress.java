package example.postal.broken;

import example.postal.PostalCode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UnconvertedAddress {
    @Id private Long id;
    private PostalCode postalCode;

    protected UnconvertedAddress() {}

    public UnconvertedAddress(Long id, PostalCode postalCode) {
        this.id = id;
        this.postalCode = postalCode;
    }
}
