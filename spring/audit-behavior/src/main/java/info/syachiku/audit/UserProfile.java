package info.syachiku.audit;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String handle;

    protected UserProfile() {
    }

    UserProfile(Long userId, String handle) {
        this.userId = userId;
        this.handle = handle;
    }
}
