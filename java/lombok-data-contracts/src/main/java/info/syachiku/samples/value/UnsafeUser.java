package info.syachiku.samples.value;

import lombok.Data;

@Data
public final class UnsafeUser {
    private final String userId;
    private String displayName;
    private String passwordHash;
}
