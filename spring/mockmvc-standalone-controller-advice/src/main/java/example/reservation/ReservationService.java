package example.reservation;

import org.springframework.stereotype.Service;

@Service
class ReservationService {
    void reserve(String roomId) {
        if ("R-100".equals(roomId)) {
            throw new ReservationConflict(roomId);
        }
    }

    static final class ReservationConflict extends RuntimeException {
        private final String roomId;

        ReservationConflict(String roomId) {
            super("Room is already reserved: " + roomId);
            this.roomId = roomId;
        }

        String roomId() {
            return roomId;
        }
    }
}
