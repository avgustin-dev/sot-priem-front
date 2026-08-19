package kg.sot.reception.dto;

import kg.sot.reception.model.NotificationChannel;

import java.time.OffsetDateTime;

public record NotificationItem(
        String id,
        OffsetDateTime at,
        NotificationChannel channel,
        String title,
        String body,
        boolean read
) {
}
