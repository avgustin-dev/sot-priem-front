package kg.sot.reception.dto;

import java.util.List;
import java.util.Map;

public record PublicBootstrap(
        Map<String, Object> site,
        List<EligibilityNode> eligibilityTree,
        CalendarSettingsDto calendar,
        SurveyBundle survey
) {
}
