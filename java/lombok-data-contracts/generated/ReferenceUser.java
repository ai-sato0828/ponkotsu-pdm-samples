package info.syachiku.samples.value;

public final class ReferenceUser {
    private String userId;
    private String displayName;

    public String getUserId() {
        return this.userId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
}
