package info.syachiku.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "unique_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_account_email", columnNames = "email"))
public class UniqueAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    protected UniqueAccount() {}
    public UniqueAccount(String email) { this.email = email; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
}
