package kg.sot.reception.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record SubmitSurveyRequest(
        String courtName,
        @NotEmpty Map<String, SurveyAnswerValue> answers
) {
}
