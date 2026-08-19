package kg.sot.reception.dto;

import jakarta.validation.constraints.NotNull;
import kg.sot.reception.model.AssignmentStatus;

public record SetAssignmentStatusRequest(@NotNull AssignmentStatus status) {
}
