package kg.sot.reception.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record SurveyResponseDto(
        String id,
        OffsetDateTime at,
        String courtName,
        Map<String, SurveyAnswerValue> answers
) {
}
