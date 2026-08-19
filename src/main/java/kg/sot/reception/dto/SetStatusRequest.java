package kg.sot.reception.dto;

import jakarta.validation.constraints.NotNull;
import kg.sot.reception.model.AppointmentStatus;

public record SetStatusRequest(@NotNull AppointmentStatus status, String note) {
}
