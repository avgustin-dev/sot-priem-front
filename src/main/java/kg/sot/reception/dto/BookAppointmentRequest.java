package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.sot.reception.model.AppealCategory;

import java.time.LocalDate;
import java.util.List;

public record BookAppointmentRequest(
        @NotBlank String fullName,
        @NotBlank String phone,
        String email,
        @NotBlank String topic,
        @NotNull AppealCategory category,
        String description,
        @NotNull LocalDate date,
        @NotBlank String slotStart,
        @NotBlank String slotEnd,
        @NotBlank String targetId,
        @Size(max = 2) List<Companion> companions
) {
}
