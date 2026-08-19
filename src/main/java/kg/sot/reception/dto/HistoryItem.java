package kg.sot.reception.dto;

import java.time.OffsetDateTime;

public record HistoryItem(OffsetDateTime at, String action, String detail) {
}
