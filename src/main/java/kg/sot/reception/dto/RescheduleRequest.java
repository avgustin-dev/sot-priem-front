package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RescheduleRequest(
        @NotNull LocalDate date,
        @NotBlank String slotStart,
        @NotBlank String slotEnd
) {
}
