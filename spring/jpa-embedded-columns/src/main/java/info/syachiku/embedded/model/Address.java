package info.syachiku.embedded.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    @Column(name = "postal_code", nullable = false, length = 8)
    private String postalCode;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "street", nullable = false, length = 160)
    private String street;

    protected Address() {}

    public Address(String postalCode, String city, String street) {
        this.postalCode = postalCode;
        this.city = city;
        this.street = street;
    }

    public String postalCode() {
        return postalCode;
    }

    public String city() {
        return city;
    }

    public String street() {
        return street;
    }
}
