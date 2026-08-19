package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class IntegerListConverter extends AbstractJsonAttributeConverter<List<Integer>> {
    public IntegerListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
