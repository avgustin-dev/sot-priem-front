package kg.sot.reception.util;

import kg.sot.reception.dto.CalendarSettingsDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Порт targets.ts resolveTargetWindow: сперва ищем получателя в site.leadership (CMS),
 * затем — жёстко заданный фолбэк (совпадает с TARGET_WINDOWS на фронте), иначе — общий график.
 */
public final class TargetWindowResolver {

    private static final Map<String, TargetWindow> FALLBACK = Map.of(
            "chairman", new TargetWindow(List.of(4), 9 * 60, 12 * 60),
            "deputy_bakirova", new TargetWindow(List.of(2), 15 * 60, 16 * 60),
            "deputy_kamchybekov", new TargetWindow(List.of(4), 9 * 60, 10 * 60)
    );

    private TargetWindowResolver() {
    }

    public static TargetWindow resolve(String targetId, CalendarSettingsDto calendar, Map<String, Object> siteContent) {
        TargetWindow calendarWide = new TargetWindow(
                calendar.receptionWeekdays(), calendar.dayStartMinutes(), calendar.dayEndMinutes());

        Map<String, Object> person = findLeadershipPerson(targetId, siteContent);
        if (person != null) {
            String windowKind = asString(person.get("windowKind"));
            if ("calendar".equals(windowKind)) {
                return calendarWide;
            }
            List<Integer> weekdays = asIntList(person.get("weekdays"));
            if (weekdays == null || weekdays.isEmpty()) weekdays = calendar.receptionWeekdays();
            Integer start = asInt(person.get("startMinutes"));
            Integer end = asInt(person.get("endMinutes"));
            return new TargetWindow(
                    weekdays,
                    start != null ? start : calendar.dayStartMinutes(),
                    end != null ? end : calendar.dayEndMinutes());
        }

        TargetWindow fallback = FALLBACK.get(targetId);
        return fallback != null ? fallback : calendarWide;
    }

    /** Короткое имя получателя записи для текстов уведомлений (targets.ts targetShort). */
    public static String shortLabel(String targetId, Map<String, Object> siteContent) {
        Map<String, Object> person = findLeadershipPerson(targetId, siteContent);
        if (person == null) return targetId;
        String shortRu = asString(person.get("shortRu"));
        if (shortRu != null && !shortRu.isBlank()) return shortRu;
        String fullNameRu = asString(person.get("fullNameRu"));
        if (fullNameRu != null && !fullNameRu.isBlank()) return fullNameRu;
        return targetId;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findLeadershipPerson(String targetId, Map<String, Object> siteContent) {
        if (siteContent == null) return null;
        Object raw = siteContent.get("leadership");
        if (!(raw instanceof List<?> list)) return null;
        for (Object item : list) {
            if (item instanceof Map<?, ?> map && Objects.equals(targetId, map.get("id"))) {
                return (Map<String, Object>) map;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInt(Object value) {
        return value instanceof Number n ? n.intValue() : null;
    }

    private static List<Integer> asIntList(Object value) {
        if (!(value instanceof List<?> list)) return null;
        List<Integer> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Number n) out.add(n.intValue());
        }
        return out;
    }
}
