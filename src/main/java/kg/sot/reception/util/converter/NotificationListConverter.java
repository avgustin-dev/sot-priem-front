package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.NotificationItem;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class NotificationListConverter extends AbstractJsonAttributeConverter<List<NotificationItem>> {
    public NotificationListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
