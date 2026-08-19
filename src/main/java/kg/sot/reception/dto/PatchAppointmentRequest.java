package kg.sot.reception.dto;

import kg.sot.reception.model.AppealCategory;

public record PatchAppointmentRequest(
        String fullName,
        String phone,
        String email,
        String topic,
        AppealCategory category,
        String description
) {
}
