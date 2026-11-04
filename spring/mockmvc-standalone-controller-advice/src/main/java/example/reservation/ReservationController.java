package example.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReservationController {
    private final ReservationService service;

    ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("/rooms/{roomId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    ReservationResult reserve(@PathVariable("roomId") String roomId) {
        service.reserve(roomId);
        return new ReservationResult(roomId, "RESERVED");
    }

    record ReservationResult(String roomId, String status) {}
}
