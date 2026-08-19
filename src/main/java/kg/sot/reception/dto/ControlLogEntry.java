package kg.sot.reception.dto;

import java.time.OffsetDateTime;

public record ControlLogEntry(
        String id,
        OffsetDateTime at,
        String authorId,
        String authorName,
        String action,
        String comment
) {
}
