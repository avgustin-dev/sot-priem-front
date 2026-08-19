package kg.sot.reception.dto;

public record ConfirmRequest(String note) {
    public ConfirmRequest() {
        this(null);
    }
}
