package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CitizenActionRequest(
        @NotBlank String pin,
        @NotBlank String action,
        LocalDate date,
        String slotStart,
        String slotEnd
) {
}
