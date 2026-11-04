package example.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ReservationAdvice {
    @ExceptionHandler(ReservationService.ReservationConflict.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ReservationError handleConflict(ReservationService.ReservationConflict exception) {
        return new ReservationError("RESERVATION_CONFLICT", exception.roomId());
    }

    record ReservationError(String code, String roomId) {}
}
