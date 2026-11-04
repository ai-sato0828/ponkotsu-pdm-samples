package info.syachiku.auditvalidation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class TaskController {
    @PostMapping("/tasks")
    ResponseEntity<Void> create(@Valid @RequestBody TaskInput input) {
        return ResponseEntity.noContent().build();
    }
}
