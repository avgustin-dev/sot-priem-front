package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class StringListConverter extends AbstractJsonAttributeConverter<List<String>> {
    public StringListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
