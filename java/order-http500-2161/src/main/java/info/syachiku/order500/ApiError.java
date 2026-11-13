package info.syachiku.order500;

public record ApiError(int status, String code, String message, String requestId) {
}
