package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.EligibilityNode;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class EligibilityNodeListConverter extends AbstractJsonAttributeConverter<List<EligibilityNode>> {
    public EligibilityNodeListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
