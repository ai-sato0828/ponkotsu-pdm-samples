package info.syachiku.auditvalidation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class RequestDatesController {
    @PostMapping("/request-dates")
    ResponseEntity<Void> check(@Valid @RequestBody RequestDates request) {
        return ResponseEntity.noContent().build();
    }
}
