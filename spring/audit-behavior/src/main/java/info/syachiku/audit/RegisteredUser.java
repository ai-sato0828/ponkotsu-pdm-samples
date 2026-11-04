package info.syachiku.audit;

import jakarta.persistence.*;

@Entity
@Table(name = "registered_users")
public class RegisteredUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected RegisteredUser() {
    }

    RegisteredUser(String name) {
        this.name = name;
    }

    Long getId() {
        return id;
    }
}
