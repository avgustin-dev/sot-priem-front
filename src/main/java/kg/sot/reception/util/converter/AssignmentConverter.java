package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.Assignment;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

@Converter
public class AssignmentConverter extends AbstractJsonAttributeConverter<Assignment> {
    public AssignmentConverter() {
        super(new TypeReference<>() {
        }, null);
    }
}
