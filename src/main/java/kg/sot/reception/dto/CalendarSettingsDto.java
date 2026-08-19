package kg.sot.reception.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CalendarSettingsDto(
        @NotNull List<Integer> receptionWeekdays,
        @NotNull Integer dayStartMinutes,
        @NotNull Integer dayEndMinutes,
        @NotNull Integer slotDurationMinutes,
        @NotNull Integer breakMinutes,
        @NotNull Integer bookingHorizonDays,
        @NotNull List<LocalDate> closedDates,
        @NotNull List<LocalDate> extraOpenDates,
        String rulesText
) {
}
