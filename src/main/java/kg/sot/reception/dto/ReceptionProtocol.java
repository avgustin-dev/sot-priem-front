package kg.sot.reception.dto;

import java.time.OffsetDateTime;

public record ReceptionProtocol(
        OffsetDateTime heldAt,
        String heldBy,
        String citizenStatement,
        String leadershipExplanation,
        String assignmentText,
        String responsibleUserId,
        String responsibleName,
        String specialistsInvolved,
        String notes
) {
}
