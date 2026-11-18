package example.requestparam;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class InputValidationAdvice {
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> invalidParameter(HandlerMethodValidationException failure) {
        if (failure.isForReturnValue()) {
            throw failure;
        }
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_PARAMETER"));
    }
}
