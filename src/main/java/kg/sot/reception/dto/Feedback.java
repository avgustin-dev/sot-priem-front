package kg.sot.reception.dto;

import java.time.OffsetDateTime;

public record Feedback(
        Integer respectful,
        Integer clearNextSteps,
        Integer convenient,
        Integer deadlinesMet,
        String comment,
        OffsetDateTime submittedAt
) {
}
