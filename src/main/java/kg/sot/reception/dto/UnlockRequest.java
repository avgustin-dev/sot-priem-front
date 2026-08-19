package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

public record UnlockRequest(@NotBlank String pin) {
}
