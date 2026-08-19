package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.ControlLogEntry;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class ControlLogListConverter extends AbstractJsonAttributeConverter<List<ControlLogEntry>> {
    public ControlLogListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
