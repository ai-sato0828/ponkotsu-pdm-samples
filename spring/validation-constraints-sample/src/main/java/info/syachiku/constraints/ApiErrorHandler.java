package info.syachiku.constraints;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handle(MethodArgumentNotValidException exception) {
        List<FieldIssue> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldIssue(error.getField(), error.getCode(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::constraint))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorBody(errors));
    }

    public record ErrorBody(List<FieldIssue> errors) {}
    public record FieldIssue(String field, String constraint, String message) {}
}
