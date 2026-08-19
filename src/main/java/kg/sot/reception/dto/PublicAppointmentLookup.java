package kg.sot.reception.dto;

import kg.sot.reception.model.AppealStage;

public record PublicAppointmentLookup(
        PublicAppointment appointment,
        AppealStage appealStage,
        Feedback feedback,
        LatestNotification latestNotification
) {
}
