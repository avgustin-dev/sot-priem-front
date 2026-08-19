package kg.sot.reception.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailableDatesResponse(String targetId, List<LocalDate> dates) {
}
