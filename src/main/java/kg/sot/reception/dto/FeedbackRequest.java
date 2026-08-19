package kg.sot.reception.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotNull @Min(1) @Max(5) Integer respectful,
        @NotNull @Min(1) @Max(5) Integer clearNextSteps,
        @NotNull @Min(1) @Max(5) Integer convenient,
        @NotNull @Min(1) @Max(5) Integer deadlinesMet,
        String comment
) {
}
