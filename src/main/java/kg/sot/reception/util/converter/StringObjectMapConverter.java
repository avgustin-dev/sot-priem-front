package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public class StringObjectMapConverter extends AbstractJsonAttributeConverter<Map<String, Object>> {
    public StringObjectMapConverter() {
        super(new TypeReference<>() {
        }, new LinkedHashMap<>());
    }
}
