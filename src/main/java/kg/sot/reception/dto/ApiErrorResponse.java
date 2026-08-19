package kg.sot.reception.dto;

public record ApiErrorResponse(int status, String code, String message) {
}
