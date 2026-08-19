package kg.sot.reception.dto;

import kg.sot.reception.model.AssignmentStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Assignment(
        String text,
        String responsibleUserId,
        String responsibleName,
        LocalDate dueDate,
        AssignmentStatus status,
        OffsetDateTime createdAt
) {
}
