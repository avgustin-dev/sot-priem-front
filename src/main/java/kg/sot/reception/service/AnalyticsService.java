package kg.sot.reception.service;

import kg.sot.reception.dto.AnalyticsResponse;
import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.AppealCategory;
import kg.sot.reception.model.AppealStage;
import kg.sot.reception.repository.AppealCardRepository;
import kg.sot.reception.util.PhoneUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AppealCardRepository appealCardRepository;

    public AnalyticsService(AppealCardRepository appealCardRepository) {
        this.appealCardRepository = appealCardRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics() {
        List<AppealCard> appeals = appealCardRepository.findAll();

        Map<String, Long> byStage = new LinkedHashMap<>();
        for (AppealStage s : AppealStage.values()) byStage.put(s.name(), 0L);
        for (AppealCard a : appeals) byStage.merge(a.getStage().name(), 1L, Long::sum);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (AppealCategory c : AppealCategory.values()) byCategory.put(c.name(), 0L);
        for (AppealCard a : appeals) byCategory.merge(a.getCategory().name(), 1L, Long::sum);

        List<AppealCard> withFeedback = appeals.stream().filter(a -> a.getFeedback() != null).toList();
        double respectful = withFeedback.stream().mapToInt(a -> a.getFeedback().respectful()).average().orElse(0);
        double clearNextSteps = withFeedback.stream().mapToInt(a -> a.getFeedback().clearNextSteps()).average().orElse(0);
        double convenient = withFeedback.stream().mapToInt(a -> a.getFeedback().convenient()).average().orElse(0);
        double deadlinesMet = withFeedback.stream().mapToInt(a -> a.getFeedback().deadlinesMet()).average().orElse(0);
        double overall = withFeedback.isEmpty() ? 0 : (respectful + clearNextSteps + convenient + deadlinesMet) / 4.0;
        AnalyticsResponse.Quality quality = new AnalyticsResponse.Quality(
                withFeedback.size(), round1(respectful), round1(clearNextSteps), round1(convenient),
                round1(deadlinesMet), round1(overall));

        Map<String, List<AppealCard>> byCitizen = appeals.stream()
                .collect(Collectors.groupingBy(a -> PhoneUtil.last9(a.getPhone())));
        List<AnalyticsResponse.RepeatedCitizen> repeated = byCitizen.values().stream()
                .filter(list -> list.size() > 1)
                .map(list -> {
                    AppealCard latest = list.stream()
                            .max(Comparator.comparing(AppealCard::getCreatedAt))
                            .orElse(list.get(0));
                    List<String> themes = list.stream().map(AppealCard::getTopic).distinct().toList();
                    List<String> codes = list.stream().map(AppealCard::getCode).toList();
                    List<String> ids = list.stream().map(AppealCard::getId).toList();
                    return new AnalyticsResponse.RepeatedCitizen(
                            latest.getFullName(), latest.getPhone(), list.size(), themes, codes, ids);
                })
                .sorted(Comparator.comparingLong(AnalyticsResponse.RepeatedCitizen::count).reversed())
                .toList();

        Map<String, Long> themeCounts = new LinkedHashMap<>();
        for (AppealCard a : appeals) {
            String theme = a.getTopic() == null || a.getTopic().isBlank() ? "—" : a.getTopic();
            themeCounts.merge(theme, 1L, Long::sum);
        }
        List<AnalyticsResponse.ThemeCount> topThemes = themeCounts.entrySet().stream()
                .map(e -> new AnalyticsResponse.ThemeCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(AnalyticsResponse.ThemeCount::count).reversed())
                .limit(10)
                .collect(Collectors.toCollection(ArrayList::new));

        return new AnalyticsResponse(byStage, byCategory, quality, repeated, topThemes);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
