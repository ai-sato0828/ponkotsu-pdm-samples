package example.booking;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomScheduleTest {
    @Test
    void anIdenticalSlotMayPassInOneRoomAndFailInAnother() {
        TimeSlot candidate = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 30));

        assertFalse(new RoomSchedule(60, List.of()).canReserve(candidate));
        assertTrue(new RoomSchedule(120, List.of()).canReserve(candidate));
    }

    @Test
    void exactLimitAndAdjacentReservationAreAllowed() {
        TimeSlot candidate = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimeSlot reserved = new TimeSlot(LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertTrue(new RoomSchedule(60, List.of(reserved)).canReserve(candidate));
    }

    @Test
    void rejectsOverlapEvenWhenTheCandidateFitsTheTimeLimit() {
        TimeSlot candidate = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimeSlot reserved = new TimeSlot(LocalTime.of(9, 30), LocalTime.of(10, 30));

        assertFalse(new RoomSchedule(60, List.of(reserved)).canReserve(candidate));
    }
}
