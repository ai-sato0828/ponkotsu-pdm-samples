package info.syachiku.auditvalidation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ValidationErrorAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        var result = exception.getBindingResult();
        Map<String, String> fields = new LinkedHashMap<>();
        result.getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var globalErrors = result.getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", fields, globalErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleMalformedJson() {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_JSON", Map.of(), List.of()));
    }
}

record ApiError(String code, Map<String, String> fields, List<String> globalErrors) {
}
