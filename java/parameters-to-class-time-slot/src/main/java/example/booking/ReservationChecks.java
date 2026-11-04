package example.booking;

import java.time.Duration;
import java.time.LocalTime;

final class ReservationChecks {
    static Duration duration(LocalTime start, LocalTime end) {
        requireSameDaySlot(start, end);
        return Duration.between(start, end);
    }

    static boolean overlaps(LocalTime start, LocalTime end,
                            LocalTime otherStart, LocalTime otherEnd) {
        requireSameDaySlot(start, end);
        requireSameDaySlot(otherStart, otherEnd);
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    private static void requireSameDaySlot(LocalTime start, LocalTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("開始時刻は終了時刻より前にしてください");
        }
    }
}
