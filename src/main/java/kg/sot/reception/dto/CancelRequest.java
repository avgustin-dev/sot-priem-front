package kg.sot.reception.dto;

public record CancelRequest(String reason) {
    public CancelRequest() {
        this(null);
    }
}
