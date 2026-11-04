package example.booking;

import java.time.Duration;
import java.util.List;

final class RoomSchedule {
    private final Duration maxDuration;
    private final List<TimeSlot> reserved;

    RoomSchedule(int maxMinutes, List<TimeSlot> reserved) {
        if (maxMinutes <= 0) {
            throw new IllegalArgumentException("予約上限は1分以上にしてください");
        }
        this.maxDuration = Duration.ofMinutes(maxMinutes);
        this.reserved = List.copyOf(reserved);
    }

    boolean canReserve(TimeSlot candidate) {
        if (candidate.duration().compareTo(maxDuration) > 0) {
            return false;
        }
        return reserved.stream().noneMatch(candidate::overlaps);
    }
}
