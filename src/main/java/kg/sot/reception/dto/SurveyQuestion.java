package kg.sot.reception.dto;

import kg.sot.reception.model.SurveyQuestionType;

import java.util.List;

public record SurveyQuestion(
        String id,
        int order,
        SurveyQuestionType type,
        boolean required,
        boolean enabled,
        String textRu,
        String textKy,
        List<SurveyOption> options,
        SurveyShowIf showIf
) {
}
