package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.HistoryItem;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class HistoryListConverter extends AbstractJsonAttributeConverter<List<HistoryItem>> {
    public HistoryListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
