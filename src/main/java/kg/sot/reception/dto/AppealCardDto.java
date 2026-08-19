package kg.sot.reception.dto;

import kg.sot.reception.model.AppealCategory;
import kg.sot.reception.model.AppealStage;

import java.time.OffsetDateTime;
import java.util.List;

public record AppealCardDto(
        String id,
        String appointmentId,
        String code,
        String fullName,
        String phone,
        String email,
        String topic,
        AppealCategory category,
        String summary,
        AppealStage stage,
        List<String> previousAppealIds,
        String previousNotes,
        String prepNotes,
        String prepCompletedBy,
        OffsetDateTime prepCompletedAt,
        ReceptionProtocol receptionProtocol,
        Assignment assignment,
        List<ControlLogEntry> controlLog,
        String finalAnswer,
        OffsetDateTime finalAnswerAt,
        Feedback feedback,
        List<NotificationItem> notifications,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
