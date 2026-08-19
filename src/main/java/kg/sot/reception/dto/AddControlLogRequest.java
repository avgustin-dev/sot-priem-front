package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

public record AddControlLogRequest(@NotBlank String action, @NotBlank String comment) {
}
