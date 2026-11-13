package info.syachiku.order500;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest webRequest) {
        HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
        String requestId = RequestIdFilter.current(request);
        boolean serverError = status.is5xxServerError();
        if (serverError) {
            log.error("operation=request result=failed path={} requestId={}",
                    request.getRequestURI(), requestId, exception);
        }
        ApiError safeBody = new ApiError(status.value(),
                serverError ? "INTERNAL_ERROR" : "INVALID_REQUEST",
                serverError ? "処理を完了できませんでした" : "入力内容を確認してください", requestId);
        // 405のAllowなど、Springが決めたstatusと必要headerを維持する。
        return new ResponseEntity<>(safeBody, headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        String requestId = RequestIdFilter.current(request);
        log.error("operation=createOrder result=failed path={} requestId={}",
                request.getRequestURI(), requestId, exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "INTERNAL_ERROR", "処理を完了できませんでした", requestId));
    }
}
