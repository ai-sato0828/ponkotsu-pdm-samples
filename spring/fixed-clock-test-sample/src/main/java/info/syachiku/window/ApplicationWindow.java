package info.syachiku.window;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationWindow {
    private final Clock clock;
    private final Instant closesAt;

    public ApplicationWindow(Clock clock, @Value("${application.closes-at}") String closesAt) {
        this.clock = clock;
        this.closesAt = Instant.parse(closesAt);
    }

    public Status status() {
        return new Status(clock.instant().isBefore(closesAt), closesAt);
    }

    public record Status(boolean accepting, Instant closesAt) {
    }
}
