package info.syachiku.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    protected Account() {}
    public Account(String email) { this.email = email; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
}
