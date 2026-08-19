package kg.sot.reception.dto;

import java.time.LocalDate;
import java.util.List;

public record SlotDayResponse(LocalDate date, String targetId, List<TimeSlot> slots) {
}
