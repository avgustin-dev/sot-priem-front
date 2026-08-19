package kg.sot.reception.dto;

import kg.sot.reception.model.AppealCategory;
import kg.sot.reception.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PublicAppointment(
        String id,
        String code,
        String fullName,
        String phone,
        String email,
        String topic,
        AppealCategory category,
        String description,
        LocalDate date,
        String slotStart,
        String slotEnd,
        AppointmentStatus status,
        String targetId,
        List<Companion> companions,
        String reviewNote,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<HistoryItem> history
) {
}
