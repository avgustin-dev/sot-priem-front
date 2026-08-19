package kg.sot.reception.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static ApiException slotUnavailable(String message) {
        return new ApiException(HttpStatus.CONFLICT, "SLOT_UNAVAILABLE", message);
    }

    public static ApiException invalidPin(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INVALID_PIN", message);
    }

    public static ApiException notCancellable(String message) {
        return new ApiException(HttpStatus.CONFLICT, "NOT_CANCELLABLE", message);
    }

    public static ApiException ineligible(String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INELIGIBLE", message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
