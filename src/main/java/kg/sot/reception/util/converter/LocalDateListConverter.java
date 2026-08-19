package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.time.LocalDate;
import java.util.List;

@Converter
public class LocalDateListConverter extends AbstractJsonAttributeConverter<List<LocalDate>> {
    public LocalDateListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
