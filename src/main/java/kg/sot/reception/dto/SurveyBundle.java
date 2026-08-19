package kg.sot.reception.dto;

import java.util.List;

public record SurveyBundle(SurveyMeta meta, List<SurveyQuestion> questions) {
}
