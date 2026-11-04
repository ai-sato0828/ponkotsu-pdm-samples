package info.syachiku.maintenance;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = HeaderAndFormController.class)
class HeaderErrorAdvice {
    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<Map<String, String>> missingHeader() {
        return ResponseEntity.badRequest().body(Map.of("code", "MISSING_HEADER"));
    }
}
