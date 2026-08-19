package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

public record CompleteReceptionRequest(
        @NotBlank String citizenStatement,
        @NotBlank String leadershipExplanation,
        @NotBlank String assignmentText,
        @NotBlank String responsibleUserId,
        @NotBlank String responsibleName,
        String specialistsInvolved,
        String notes
) {
}
