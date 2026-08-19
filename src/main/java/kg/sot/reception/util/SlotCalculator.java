package kg.sot.reception.util;

import kg.sot.reception.dto.CalendarSettingsDto;
import kg.sot.reception.dto.TimeSlot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Порт бизнес-логики slots.ts (генерация расписания, доступные даты/слоты). */
public final class SlotCalculator {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private SlotCalculator() {
    }

    public static String formatDateRu(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    public static String minutesToTime(int total) {
        int h = total / 60;
        int m = total % 60;
        return String.format("%02d:%02d", h, m);
    }

    public static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /** 0=вс … 6=сб, как Date.getDay() в JS. */
    public static int jsWeekday(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7;
    }

    public static List<TimeSlot> generateDaySlots(int slotDurationMinutes, int breakMinutes, int startMinutes, int endMinutes) {
        List<TimeSlot> slots = new ArrayList<>();
        int cursor = startMinutes;
        while (cursor + slotDurationMinutes <= endMinutes) {
            String start = minutesToTime(cursor);
            String end = minutesToTime(cursor + slotDurationMinutes);
            slots.add(new TimeSlot(start, end, start + " – " + end));
            cursor += slotDurationMinutes + breakMinutes;
        }
        return slots;
    }

    public static boolean isReceptionDate(LocalDate date, CalendarSettingsDto calendar, TargetWindow window) {
        if (calendar.closedDates().contains(date)) return false;
        if (calendar.extraOpenDates().contains(date)) return true;
        return window.weekdays().contains(jsWeekday(date));
    }

    public static List<LocalDate> listAvailableDates(CalendarSettingsDto calendar, LocalDate from, TargetWindow window) {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i <= calendar.bookingHorizonDays(); i++) {
            LocalDate day = from.plusDays(i);
            if (isReceptionDate(day, calendar, window)) dates.add(day);
        }
        return dates;
    }

    public static List<TimeSlot> availableSlots(
            LocalDate date,
            CalendarSettingsDto calendar,
            TargetWindow window,
            Set<String> bookedSlotStarts,
            boolean isToday,
            int nowMinutes
    ) {
        if (!isReceptionDate(date, calendar, window)) return List.of();
        List<TimeSlot> all = generateDaySlots(
                calendar.slotDurationMinutes(), calendar.breakMinutes(), window.startMinutes(), window.endMinutes());
        List<TimeSlot> free = new ArrayList<>();
        for (TimeSlot slot : all) {
            if (bookedSlotStarts.contains(slot.start())) continue;
            if (isToday && timeToMinutes(slot.start()) <= nowMinutes) continue;
            free.add(slot);
        }
        return free;
    }
}
