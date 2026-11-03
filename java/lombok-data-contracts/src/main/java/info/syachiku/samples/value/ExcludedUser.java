package info.syachiku.samples.value;

import lombok.Data;

@Data
public final class ExcludedUser {
    private final String userId;
    private String displayName;
    @lombok.ToString.Exclude
    private String passwordHash;
}
