package example.reservation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceTest {
    private final ReservationService service = new ReservationService();

    @Test
    void rejectsReservedRoom() {
        var exception = assertThrows(ReservationService.ReservationConflict.class,
                () -> service.reserve("R-100"));
        assertEquals("R-100", exception.roomId());
    }

    @Test
    void allowsFreeRoom() {
        assertDoesNotThrow(() -> service.reserve("R-200"));
    }
}
