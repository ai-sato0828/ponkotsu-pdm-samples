package example.status;

import java.util.Arrays;
import java.util.Optional;

public enum Status {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public static Optional<Status> fromValue(String input) {
        return Arrays.stream(values())
                .filter(status -> status.value.equals(input))
                .findFirst();
    }
}
