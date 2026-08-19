package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kg.sot.reception.model.AppealCategory;

public record CompletePrepRequest(
        @NotBlank String summary,
        String prepNotes,
        @NotNull AppealCategory category
) {
}
