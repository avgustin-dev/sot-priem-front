package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.Feedback;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

@Converter
public class FeedbackConverter extends AbstractJsonAttributeConverter<Feedback> {
    public FeedbackConverter() {
        super(new TypeReference<>() {
        }, null);
    }
}
