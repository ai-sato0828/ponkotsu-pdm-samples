package info.syachiku.samples.value;

import java.util.Objects;

public final class SafeUser {
    private final String userId;
    private String displayName;
    private String passwordHash;

    public SafeUser(String userId) {
        this.userId = Objects.requireNonNull(userId, "assigned userId is required");
    }

    public String getUserId() {
        return this.userId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SafeUser)) return false;
        final SafeUser other = (SafeUser) o;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SafeUser(userId=" + this.getUserId() + ")";
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
