package kg.sot.reception.util;

import java.util.List;

/** Окно приёма конкретного получателя записи (weekdays: 0=вс … 6=сб; минуты от 00:00). */
public record TargetWindow(List<Integer> weekdays, int startMinutes, int endMinutes) {
}
