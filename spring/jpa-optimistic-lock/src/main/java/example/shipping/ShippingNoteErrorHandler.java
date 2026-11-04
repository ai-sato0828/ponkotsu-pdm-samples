package example.shipping;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ShippingNotesController.class)
class ShippingNoteErrorHandler {

    @ExceptionHandler({
        ShippingNoteService.StaleShippingNoteException.class,
        OptimisticLockingFailureException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    ConflictResponse conflict() {
        return new ConflictResponse("STALE_SHIPPING_NOTE");
    }

    record ConflictResponse(String code) {
    }
}
