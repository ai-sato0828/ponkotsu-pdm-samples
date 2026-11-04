package example;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public final class DeadlinePolicy {
    private final Clock clock;

    public DeadlinePolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public boolean canSubmit(LocalDate deadline) {
        Objects.requireNonNull(deadline);
        LocalDate today = LocalDate.now(clock);
        return !today.isAfter(deadline);
    }
}
