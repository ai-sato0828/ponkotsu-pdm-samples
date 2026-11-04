package example.booking;

import java.time.Duration;
import java.time.LocalTime;

public record TimeSlot(LocalTime start, LocalTime end) {
    public TimeSlot {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("開始時刻は終了時刻より前にしてください");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end()) && other.start().isBefore(end);
    }
}
