package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(@NotBlank String reason) {
}
