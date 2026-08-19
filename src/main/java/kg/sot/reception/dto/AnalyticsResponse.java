package kg.sot.reception.dto;

import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
        Map<String, Long> byStage,
        Map<String, Long> byCategory,
        Quality quality,
        List<RepeatedCitizen> repeated,
        List<ThemeCount> topThemes
) {
    public record Quality(
            long count,
            double respectful,
            double clearNextSteps,
            double convenient,
            double deadlinesMet,
            double overall
    ) {
    }

    public record RepeatedCitizen(
            String name,
            String phone,
            long count,
            List<String> themes,
            List<String> codes,
            List<String> ids
    ) {
    }

    public record ThemeCount(String theme, long count) {
    }
}
