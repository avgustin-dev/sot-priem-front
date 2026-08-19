package kg.sot.reception.service;

import kg.sot.reception.dto.NotificationItem;
import kg.sot.reception.model.NotificationChannel;
import kg.sot.reception.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Реальная отправка письма — заглушка (лог). SMTP (spring-boot-starter-mail) подключён
 * в зависимостях; при готовности учётных данных реализовать через JavaMailSender.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationItem create(String citizenEmail, String title, String body) {
        NotificationChannel channel = (citizenEmail != null && !citizenEmail.isBlank())
                ? NotificationChannel.email
                : NotificationChannel.system;
        if (channel == NotificationChannel.email) {
            log.info("[EMAIL -> {}] {}: {}", citizenEmail, title, body);
        } else {
            log.info("[SYSTEM] {}: {}", title, body);
        }
        return new NotificationItem(IdGenerator.next("n"), OffsetDateTime.now(), channel, title, body, false);
    }
}
