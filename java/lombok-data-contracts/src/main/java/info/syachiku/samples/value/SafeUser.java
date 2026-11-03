package info.syachiku.samples.value;

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class SafeUser {
    @EqualsAndHashCode.Include
    @ToString.Include
    private final String userId;

    @Setter
    private String displayName;
    @Setter
    private String passwordHash;

    public SafeUser(String userId) {
        this.userId = Objects.requireNonNull(userId, "assigned userId is required");
    }
}
