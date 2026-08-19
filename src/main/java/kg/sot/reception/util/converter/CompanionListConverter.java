package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.Companion;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class CompanionListConverter extends AbstractJsonAttributeConverter<List<Companion>> {
    public CompanionListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
